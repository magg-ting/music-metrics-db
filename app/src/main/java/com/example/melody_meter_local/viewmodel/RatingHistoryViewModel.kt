package com.example.melody_meter_local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.FavoritesRepository
import com.example.melody_meter_local.repository.RatingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingHistoryViewModel @Inject constructor (
    private val repository: RatingHistoryRepository,
) : ViewModel() {

    private val _ratingHistory = MutableLiveData<List<Pair<Song, Double>>>()
    val ratingHistory: LiveData<List<Pair<Song, Double>>> get() = _ratingHistory

    fun fetchRatingHistory() {
        viewModelScope.launch {
            val records = repository.fetchRatingHistory()
            _ratingHistory.value = records
        }
    }

}