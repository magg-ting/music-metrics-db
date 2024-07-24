package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    // Initialize Firebase references
    private var userDbReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")
    private var songDbReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Songs")

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
                val favorites =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
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
                val favorites =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
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

                // Update the song DB
                val songDataSnapshot = songRef.child("ratings").get().await()
                val songRatings = songDataSnapshot.getValue(object :
                    GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {})
                    ?: mutableListOf()

                // Check if user already rated the song before
                val existingRatingByUser = songRatings.find { it.contains(uid) }
                if (existingRatingByUser != null) {
                    // Update existing rating
                    existingRatingByUser[uid] = rating
                } else {
                    // Add new rating
                    songRatings.add(mutableMapOf(uid to rating))
                }

                // Update average rating of the song
                val average = if (songRatings.isNotEmpty()) {
                    songRatings.sumOf { it.values.first() } / songRatings.size
                } else {
                    0.0
                }

                songRef.child("ratings").setValue(songRatings).await()
                songRef.child("avgRating").setValue(average).await()


                // Update the user DB
                val userDataSnapshot = userRef.child("ratings").get().await()
                val userRatings = userDataSnapshot.getValue(object :
                    GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {})
                    ?: mutableListOf()

                // Check if user already rated the song before
                val existingRatingOfSong = userRatings.find { it.contains(spotifyTrackId) }
                if (existingRatingOfSong != null) {
                    // Update existing rating
                    existingRatingOfSong[spotifyTrackId] = rating
                } else {
                    // Add new rating
                    userRatings.add(mutableMapOf(spotifyTrackId to rating))
                }

                userRef.child("ratings").setValue(userRatings).await()

                _ratingSubmissionStatus.value = true
            } catch (e: Exception) {
                _ratingSubmissionStatus.value = false
                Log.e("Song Detail View Model", "Failed to save rating", e)
            }
        }
    }


}