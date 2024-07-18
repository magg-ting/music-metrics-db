package com.example.melody_meter_local.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.User
import com.example.melody_meter_local.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class FavoritesViewModel (
    private val repository: FavoritesRepository,
    private val savedStateHandle: SavedStateHandle,
    private val user: User?,
    private val uid: String?
) : ViewModel() {

    private val _favoriteSongs = MutableLiveData<List<Song>>()
    val favoriteSongs: LiveData<List<Song>> get() = _favoriteSongs

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    init {
        uid?.let {
            fetchFavoriteSongs(it)
        }
    }

    private fun fetchFavoriteSongs(uid: String) {
        viewModelScope.launch {
            val songs = repository.fetchFavoriteSongs(uid)
            _favoriteSongs.value = songs
            _isEmpty.value = songs.isEmpty()
        }
    }
}
