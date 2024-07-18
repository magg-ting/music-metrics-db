package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.spotify.Album
import com.example.melody_meter_local.repository.SpotifyRepository
import com.example.melody_meter_local.repository.TopRatedRepository
import com.example.melody_meter_local.repository.TrendingRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val trendingRepository: TrendingRepository
) : ViewModel() {

    private val topRatedRepository = TopRatedRepository()
    private val _topRatedSongs = MutableLiveData<List<Song>>()
    val topRatedSongs: LiveData<List<Song>> get() = _topRatedSongs

    private val _trendingSongs = MutableLiveData<List<Song>>()
    val trendingSongs: LiveData<List<Song>> get() = _trendingSongs

    private val _newReleases = MutableLiveData<List<Album>>()
    val newReleases: LiveData<List<Album>> get() = _newReleases

    init {
        loadTopRatedSongs()
        fetchTrendingSongs()
        fetchNewReleases()
    }

    private fun loadTopRatedSongs() {
        viewModelScope.launch {
            val songs = topRatedRepository.fetchTopRatedSongs()
            _topRatedSongs.postValue(songs)
        }
    }

    private fun fetchTrendingSongs() {
        viewModelScope.launch {
            val songs = trendingRepository.fetchTrendingSongs()
            _trendingSongs.postValue(songs)
        }
    }

    private fun fetchNewReleases() {
        viewModelScope.launch {
            try {
                val response = spotifyRepository.getNewReleases()
                if (response.isSuccessful) {
                    val albumsResponse = response.body()?.albums
                    albumsResponse?.let {
                        _newReleases.postValue(it.items)
                    }
                } else {
                    Log.e("HomeViewModel", "Failed to fetch new releases: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching new releases", e)
            }
        }
    }

}