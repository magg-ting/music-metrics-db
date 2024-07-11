package com.example.melody_meter_local.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.melody_meter_local.data.Song

class SearchViewModel : ViewModel() {

    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> = _searchResults

    fun performSearch(query: String) {
        // Simulate fetching search results from a repository or API
        val results = listOf(
            Song("","Happy Birthday", "Anonymous", null, 10.0),
            Song("","DelayNoMore", "F**k", null, 8.8),
            Song("","Other World", "Keira", null, 6.5)
        )
        _searchResults.value = results
    }
}