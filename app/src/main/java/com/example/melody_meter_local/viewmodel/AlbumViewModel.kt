package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.spotify.Album
import com.example.melody_meter_local.repository.SpotifyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository
) : ViewModel() {
    private val _selectedAlbum = MutableLiveData<Album>()
    val selectedAlbum: LiveData<Album> get() = _selectedAlbum

    private val _albumTracks = MutableLiveData<List<Song>>()
    val albumTracks: LiveData<List<Song>> get() = _albumTracks

    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
        fetchAlbumTracks(album.spotifyAlbumId)
    }

    private fun fetchAlbumTracks(albumId: String) {
        viewModelScope.launch {
            try {
                val response = spotifyRepository.getAlbumTracks(albumId)
                if (response.isSuccessful) {
                    val tracksResponse = response.body()
                    tracksResponse?.let {
                        val album = selectedAlbum.value
                        album?.let {
                            _albumTracks.postValue(tracksResponse.toSongs(it))
                        }
                    }
                } else {
                    Log.e("AlbumViewModel", "Failed to fetch album tracks: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AlbumViewModel", "Error fetching album tracks", e)
            }
        }
    }
}