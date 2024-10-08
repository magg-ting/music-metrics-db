package com.example.music_metrics.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_metrics.model.Song
import com.example.music_metrics.repository.RatingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingHistoryViewModel @Inject constructor(
    private val repository: RatingHistoryRepository,
) : ViewModel() {

    private val _ratingHistory = MutableLiveData<List<Pair<Song, Double>>>()
    val ratingHistory: LiveData<List<Pair<Song, Double>>> get() = _ratingHistory

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading


    fun fetchRatingHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val records = repository.fetchRatingHistory()
            _ratingHistory.value = records
            _isLoading.value = false
        }
    }

}