package com.example.music_metrics.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_metrics.di.SongDatabaseReference
import com.example.music_metrics.di.UserDatabaseReference
import com.example.music_metrics.model.Song
import com.example.music_metrics.repository.SongDetailRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val repository: SongDetailRepository
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
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(spotifyTrackId)
        }
    }

    fun toggleFavorite(spotifyTrackId: String, song: Song) {
        viewModelScope.launch {
            _isFavorite.value = repository.toggleFavorite(spotifyTrackId, song)
        }
    }

    fun saveRating(song: Song, rating: Double) {
        viewModelScope.launch {
            val success = repository.saveRating(song, rating)
            _ratingSubmissionStatus.value = success
            if (success) {
                fetchSongDetails(song.spotifyTrackId)
            }
        }
    }

    fun fetchSongDetails(spotifyTrackId: String) {
        viewModelScope.launch {
            _songDetails.value = repository.getSongDetails(spotifyTrackId)
        }
    }

    fun clearState() {
        _isFavorite.value = null
        _ratingSubmissionStatus.value = null
        _songDetails.value = null
    }
}