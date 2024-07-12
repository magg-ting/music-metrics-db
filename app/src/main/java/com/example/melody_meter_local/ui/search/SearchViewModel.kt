package com.example.melody_meter_local.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.SpotifyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository
): ViewModel(){

    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> get() = _searchResults

    fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    spotifyRepository.search(query)
                }
                if (response.isSuccessful) {
                    val spotifyResponse = response.body()
                    spotifyResponse?.let { res ->
                        val results = mutableListOf<Song>()
                        res.tracks?.items?.forEach { track ->
                            results.add(
                                Song(
                                    spotifyTrackId = track.id,
                                    name = track.name,
                                    artist = track.artists.joinToString(", ") { it.name },
                                    album = track.album.name,
                                    imgUrl = track.album.images.firstOrNull()?.url
                                )
                            )
                        }
                        _searchResults.postValue(results)
                    }
                } else {
                    Log.e("SearchViewModel", "Search failed with response: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed: ${e.message}")
            }
        }
    }
}
