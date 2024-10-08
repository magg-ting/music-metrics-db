package com.example.music_metrics.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_metrics.model.Song
import com.example.music_metrics.model.spotify.Album
import com.example.music_metrics.repository.SpotifyRepository
import com.example.music_metrics.repository.TopRatedRepository
import com.example.music_metrics.repository.TrendingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val topRatedRepository: TopRatedRepository,
    private val spotifyRepository: SpotifyRepository,
    private val trendingRepository: TrendingRepository
) : ViewModel() {

    private val _topRatedSongs = MutableLiveData<List<Song>>()
    val topRatedSongs: LiveData<List<Song>> get() = _topRatedSongs

    private val _trendingSongs = MutableLiveData<List<Song>>()
    val trendingSongs: LiveData<List<Song>> get() = _trendingSongs

    private val _newReleases = MutableLiveData<List<Album>>()
    val newReleases: LiveData<List<Album>> get() = _newReleases

    private val _albumTracks = MutableLiveData<List<Song>>()
    val albumTracks: LiveData<List<Song>> get() = _albumTracks

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var pendingOperations = 3 // Number of operations to track

    init {
        _isLoading.value = true
        fetchTopRatedSongs()
        fetchTrendingSongs()
        fetchNewReleases()
    }

    fun fetchTopRatedSongs() {
        viewModelScope.launch {
            try {
                val songs = topRatedRepository.fetchTopRatedSongs()
                _topRatedSongs.postValue(songs)
            } finally {
                checkAllOperationsCompleted()
            }
        }
    }

    fun fetchTrendingSongs() {
        viewModelScope.launch {
            try {
                val songs = trendingRepository.fetchTrendingSongs()
                _trendingSongs.postValue(songs)
            } finally {
                checkAllOperationsCompleted()
            }
        }
    }

    fun fetchNewReleases() {
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
            } finally {
                checkAllOperationsCompleted()
            }
        }
    }

    private fun checkAllOperationsCompleted() {
        pendingOperations--
        if (pendingOperations <= 0) {
            _isLoading.value = false
        }
    }

    fun resetPendingOperations(){
        pendingOperations = 3
    }

}