package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.RecentSearchesRepository
import com.example.melody_meter_local.repository.SpotifyRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    // Inject the necessary dependencies
    private val spotifyRepository: SpotifyRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Observe
    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> get() = _searchResults

    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> get() = _isSearching

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> get() = _recentSearches

    // Call spotifyAPI to query and update the _searchResults live data
    fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                // Save the query to recent searches
                saveSearchQuery(query)

                val response = withContext(Dispatchers.IO) {
                    spotifyRepository.search(query)
                }
                if (response.isSuccessful) {
                    val spotifyResponse = response.body()
                    spotifyResponse?.let { res ->
                        val tracks = res.tracks?.items ?: emptyList()
                        val songs = tracks.map { it.toSong() }
                        val ratedSongs = fetchSongRatings(songs)
                        _searchResults.postValue(ratedSongs)
                    }

                } else {
                    Log.e(
                        "SearchViewModel",
                        "Search failed with response: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed: ${e.message}")
            }
        }
    }

    private suspend fun fetchSongRatings(songs: List<Song>): List<Song> {
        return withContext(Dispatchers.IO) {
            songs.map { song ->
                try {
                    val (songRatings, avgRating) = spotifyRepository.getSongRatings(song.spotifyTrackId)
                    song.copy(
                        ratings = songRatings,
                        avgRating = avgRating
                    )
                } catch (e: Exception) {
                    Log.d(
                        "SearchViewModel",
                        "Failed to fetch ratings for song: ${song.spotifyTrackId}",
                        e
                    )
                    song.copy(ratings = mutableListOf(), avgRating = 0.0)
                }
            }
        }
    }

    fun fetchRecentSearches() {
        viewModelScope.launch {
            val recentSearches = recentSearchesRepository.fetchRecentSearches()
            _recentSearches.postValue(recentSearches)
        }
    }


    // remove a recent search by the query string
    fun removeSearchQuery(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: mutableListOf()
        if (currentSearches.remove(query)) {
            updateRecentSearches(currentSearches)
            auth.currentUser?.uid ?: return
            viewModelScope.launch {
                recentSearchesRepository.removeRecentSearch(query)
            }
        }
    }

    // remove all recent searches of the user
    fun clearAllSearches() {
        auth.currentUser?.uid ?: return
        viewModelScope.launch {
            recentSearchesRepository.removeAllSearches(
                onSuccess = {
                    _recentSearches.value = emptyList() // Update UI with empty list
                },
                onFailure = { exception ->
                    Log.e("SearchViewModel", "Error removing all searches: ${exception.message}")
                })
        }

    }

    // save the query in both the user db and the popular search db
    private fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            recentSearchesRepository.saveSearchQuery(query)
            // Fetch and update recent searches after saving
            val updatedRecentSearches = recentSearchesRepository.fetchRecentSearches()
            updateRecentSearches(updatedRecentSearches)
        }
    }

    fun setIsSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
    }

    private fun updateRecentSearches(newRecentSearches: List<String>) {
        _recentSearches.value = newRecentSearches
    }

}
