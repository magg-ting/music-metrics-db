package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
) : ViewModel() {

    private val _favoriteSongs = MutableLiveData<List<Song>>()
    val favoriteSongs: LiveData<List<Song>> get() = _favoriteSongs

    init {
        fetchFavoriteSongs()
    }

    private fun fetchFavoriteSongs() {
        viewModelScope.launch {
            val favorites = repository.fetchFavoriteSongs()
            _favoriteSongs.value = favorites
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
