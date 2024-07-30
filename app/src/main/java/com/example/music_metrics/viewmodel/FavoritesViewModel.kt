package com.example.music_metrics.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_metrics.model.Song
import com.example.music_metrics.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
) : ViewModel() {

    private val _favoriteSongs = MutableLiveData<List<Song>>()
    val favoriteSongs: LiveData<List<Song>> get() = _favoriteSongs

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchFavoriteSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            val favorites = repository.fetchFavoriteSongs()
            _favoriteSongs.value = favorites
            _isLoading.value = false
        }
    }

    fun addFavorite(songId: String) {
        viewModelScope.launch {
            try {
                repository.addFavorite(songId)
                // refresh the list after adding
                //fetchFavoriteSongs()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to add favorite", e)
            }
        }
    }

    fun removeFavorite(songId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(songId)
                // Refresh the list after removing
                //fetchFavoriteSongs()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Failed to remove favorite", e)
            }
        }
    }
}
