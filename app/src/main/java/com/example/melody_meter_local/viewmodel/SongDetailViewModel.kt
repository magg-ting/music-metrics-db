package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.Song
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
    private val auth: FirebaseAuth,
    @SongDatabaseReference var songDbReference: DatabaseReference,
    @UserDatabaseReference var userDbReference: DatabaseReference
) : ViewModel() {

    // Live data for saving to favorites state
    private val _isFavorite = MutableLiveData<Boolean?>()
    val isFavorite: MutableLiveData<Boolean?> get() = _isFavorite

    // Live data for rating submission state
    private val _ratingSubmissionStatus = MutableLiveData<Boolean?>()
    val ratingSubmissionStatus: MutableLiveData<Boolean?> get() = _ratingSubmissionStatus

    // Live data for song details (since avgRating may be updated when user submits a new rating)
    private val _songDetails = MutableLiveData<Song?>()
    val songDetails: MutableLiveData<Song?> get() = _songDetails

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
                Log.e("SongDetailViewModel", "Failed to get user favorites", e)
            }
        }
    }

    fun toggleFavorite(spotifyTrackId: String, song: Song) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val songRef = songDbReference.child(spotifyTrackId)

                // check if the song is in user favorites
                val dataSnapshot = userRef.child("favorites").get().await()
                val favorites =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
                val isFavorite = if (favorites.contains(spotifyTrackId)) {
                    favorites.remove(spotifyTrackId)
                    false
                } else {
                    favorites.add(spotifyTrackId)

                    // Save the song to the song database if it does not exist
                    val songSnapshot = songRef.get().await()
                    if (!songSnapshot.exists()) {
                        songRef.setValue(song).await()
                    }

                    true
                }
                userRef.child("favorites").setValue(favorites).await()
                _isFavorite.value = isFavorite
            }
            catch (e: Exception) {
                _isFavorite.value = false
                Log.e("SongDetailViewModel", "Failed to update favorites", e)
            }
        }
    }

    fun saveRating(song: Song, rating: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val userRef = userDbReference.child(uid)
                val songRef = songDbReference.child(song.spotifyTrackId)

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

                // Update song details along with the ratings and average rating
                songRef.setValue(
                    mapOf(
                        "spotifyTrackId" to song.spotifyTrackId,
                        "name" to song.name,
                        "artist" to song.artist,
                        "album" to song.album,
                        "imgUrl" to song.imgUrl,
                        "ratings" to songRatings,
                        "avgRating" to average
                    )
                ).await()


                // Update the user DB
                val userDataSnapshot = userRef.child("ratings").get().await()
                val userRatings = userDataSnapshot.getValue(object :
                    GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {})
                    ?: mutableListOf()

                // Check if user already rated the song before
                val existingRatingOfSong = userRatings.find { it.contains(song.spotifyTrackId) }
                if (existingRatingOfSong != null) {
                    // Update existing rating
                    existingRatingOfSong[song.spotifyTrackId] = rating
                } else {
                    // Add new rating
                    userRatings.add(mutableMapOf(song.spotifyTrackId to rating))
                }

                userRef.child("ratings").setValue(userRatings).await()

                _ratingSubmissionStatus.value = true
            } catch (e: Exception) {
                _ratingSubmissionStatus.value = false
                Log.e("SongDetailViewModel", "Failed to save rating", e)
            }
        }
    }

    fun fetchSongDetails(spotifyTrackId: String) {
        viewModelScope.launch {
            try {
                val songRef = songDbReference.child(spotifyTrackId).get().await()
                val song = songRef.getValue(Song::class.java)
                song?.let {
                    _songDetails.value = it
                }
            } catch (e: Exception) {
                Log.e("SongDetailViewModel", "Failed to fetch song details", e)
            }
        }
    }

    fun clearState() {
        _isFavorite.value = null
        _ratingSubmissionStatus.value = null
        _songDetails.value = null
    }
}