package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val auth: FirebaseAuth
): ViewModel() {
    // Initialize Firebase references
    private var userDbReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private var songDbReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Songs")

    // Live data for saving to favorites state
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite

    // Live data for rating submission state
    private val _ratingSubmissionStatus = MutableLiveData<Boolean>()
    val ratingSubmissionStatus: LiveData<Boolean> get() = _ratingSubmissionStatus

    fun updateFavoriteState(spotifyTrackId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val dataSnapshot = userRef.child("favorites").get().await()
                val favorites = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                _isFavorite.value = favorites.contains(spotifyTrackId)
            } catch (e: Exception) {
                _isFavorite.value = false
                Log.e("Song Detail View Model", "Failed to get user favorites", e)
            }
        }
    }

    fun toggleFavorite(spotifyTrackId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val dataSnapshot = userRef.child("favorites").get().await()
                val favorites = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                val isFavorite = if (favorites.contains(spotifyTrackId)) {
                    favorites.remove(spotifyTrackId)
                    false
                } else {
                    favorites.add(spotifyTrackId)
                    true
                }
                userRef.child("favorites").setValue(favorites).await()
                _isFavorite.value = isFavorite
            } catch (e: Exception) {
                _isFavorite.value = false
                Log.e("Song Detail View Model", "Failed to update favorites", e)
            }
        }
    }

    fun saveRating(spotifyTrackId: String, rating: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val songRef = songDbReference.child(spotifyTrackId)

                val songDataSnapshot = songRef.child("ratings").get().await()
                val songRatings = songDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<Double>>() {}) ?: mutableListOf()
                songRatings.add(rating)
                songRef.child("ratings").setValue(songRatings).await()

                val userDataSnapshot = userRef.child("ratings").get().await()
                val userRatings = userDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()
                userRatings.add(mutableMapOf(spotifyTrackId to rating))
                userRef.child("ratings").setValue(userRatings).await()

                _ratingSubmissionStatus.value = true
            } catch (e: Exception) {
                _ratingSubmissionStatus.value = false
                Log.e("Song Detail View Model", "Failed to save rating", e)
            }
        }
    }


}