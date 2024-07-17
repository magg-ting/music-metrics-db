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
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SongDetailViewModel(private val auth: FirebaseAuth): ViewModel() {
    private lateinit var userDbReference: DatabaseReference
    private lateinit var songDbReference: DatabaseReference

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite

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
                Log.e("SongDetailFragment", "Failed to get user favorites", e)
            }
        }
    }

    fun toggleFavorite(songId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val dataSnapshot = userRef.child("favorites").get().await()
                val favorites = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                val isFavorite = if (favorites.contains(songId)) {
                    favorites.remove(songId)
                    false
                } else {
                    favorites.add(songId)
                    true
                }
                userRef.child("favorites").setValue(favorites).await()
                _isFavorite.value = isFavorite
            } catch (e: Exception) {
                _isFavorite.value = false
            }
        }
    }

    fun saveRating(songId: String, rating: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val songRef = songDbReference.child(songId)

                val songDataSnapshot = songRef.child("ratings").get().await()
                val songRatings = songDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<Double>>() {}) ?: mutableListOf()
                songRatings.add(rating)
                songRef.child("ratings").setValue(songRatings).await()

                val userDataSnapshot = userRef.child("ratings").get().await()
                val userRatings = userDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()
                userRatings.add(mutableMapOf(songId to rating))
                userRef.child("ratings").setValue(userRatings).await()

                _ratingSubmissionStatus.value = true
            } catch (e: Exception) {
                _ratingSubmissionStatus.value = false
            }
        }
    }


}