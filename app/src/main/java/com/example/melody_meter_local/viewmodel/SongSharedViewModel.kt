package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.FavoritesRepository
import com.example.melody_meter_local.repository.RatingHistoryRepository
import com.example.melody_meter_local.repository.SpotifyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongSharedViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val ratingHistoryRepository: RatingHistoryRepository,
    private val spotifyRepository: SpotifyRepository
): ViewModel()  {

    private val _favoriteSongs = MutableLiveData<List<Song>>()
    val favoriteSongs: LiveData<List<Song>> get() = _favoriteSongs

    private val _ratingHistory = MutableLiveData<List<Pair<Song, Double>>>()
    val ratingHistory: LiveData<List<Pair<Song, Double>>> get() = _ratingHistory

    init {
        fetchFavoriteSongs()
        fetchRatingHistory()
    }

    fun fetchFavoriteSongs() {
        viewModelScope.launch {
            try {
                val favorites = favoritesRepository.fetchFavoriteSongs()
                _favoriteSongs.value = favorites
            } catch (e: Exception) {
                Log.e("SongSharedViewModel", "Failed to fetch favorite songs", e)
            }
        }
    }

    fun fetchRatingHistory() {
        viewModelScope.launch {
            try {
                val ratingHistory = ratingHistoryRepository.fetchRatingHistory()
                _ratingHistory.value = ratingHistory
            } catch (e: Exception) {
                Log.e("SongSharedViewModel", "Failed to fetch rating history", e)
            }
        }
    }

    fun addFavorite(songId: String) {
        viewModelScope.launch {
            try {
                favoritesRepository.addFavorite(songId)
                fetchFavoriteSongs() // Refresh the list
            } catch (e: Exception) {
                Log.e("SongSharedViewModel", "Failed to add favorite", e)
            }
        }
    }

    fun removeFavorite(songId: String) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(songId)
                fetchFavoriteSongs() // Refresh the list
            } catch (e: Exception) {
                Log.e("SongSharedViewModel", "Failed to remove favorite", e)
            }
        }
    }

    fun saveRating(song: Song, rating: Double) {
        viewModelScope.launch {
            try {
                //ratingHistoryRepository.saveRating(song, rating)
                fetchRatingHistory() // Refresh the list
            } catch (e: Exception) {
                Log.e("SongSharedViewModel", "Failed to save rating", e)
            }
        }
    }
}