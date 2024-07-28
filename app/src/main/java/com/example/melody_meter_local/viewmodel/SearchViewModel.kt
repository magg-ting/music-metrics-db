package com.example.melody_meter_local.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.di.PopularSearchesDatabaseReference
import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.PopularSearch
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.RecentSearchesRepository
import com.example.melody_meter_local.repository.SpotifyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    // Inject the necessary dependencies
    private val spotifyRepository: SpotifyRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @PopularSearchesDatabaseReference private val popularSearchesDbReference: DatabaseReference,
    @SongDatabaseReference private val songDbReference: DatabaseReference,
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
                        "SearchViewModel", "Search failed with response: ${response.errorBody()?.string()}"
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
                    val songRef = songDbReference.child(song.spotifyTrackId)

                    val ratingsSnapshot = songRef.child("ratings").get().await()
                    val songRatings = ratingsSnapshot.getValue(object :
                        GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()

                    val avgRatingSnapshot = songRef.child("avgRating").get().await()
                    val avgRating = (avgRatingSnapshot.value as Number).toDouble()

                    // Create a new Song object with updated ratings and avgRating
                    song.copy(
                        ratings = songRatings,
                        avgRating = avgRating
                    )
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Failed to fetch ratings for song: ${song.spotifyTrackId}", e)
                    song.copy(ratings = mutableListOf(), avgRating = 0.0)
                }
            }
        }
    }

    // remove a recent search by the query string
    fun removeSearchQuery(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: mutableListOf()
        if (currentSearches.remove(query)) {
            updateRecentSearches(currentSearches)
            val uid = auth.currentUser?.uid
            if (uid != null) {
                viewModelScope.launch {
                    recentSearchesRepository.removeRecentSearch(query)
                }
            }
        }
    }

    // remove all recent searches of the user
    fun clearAllSearches(){
        val uid = auth.currentUser?.uid
        if (uid != null) {
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
    }

    // save the query in both the user db and the popular search db
    fun saveSearchQuery(query: String) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = userDbReference.child(uid)
            userRef.child("recentSearches")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val recentSearches = dataSnapshot.getValue(object :
                            GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                        if (recentSearches.contains(query)) {
                            recentSearches.remove(query)
                        }
                        recentSearches.add(0, query)
                        // keeping only 10 recent searches for simplicity
                        if (recentSearches.size > 10) {
                            recentSearches.removeAt(recentSearches.size - 1)
                        }
                        userRef.child("recentSearches").setValue(recentSearches)
                        _recentSearches.postValue(recentSearches)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(ContentValues.TAG, "Load Recent Searches: onCancelled", databaseError.toException())
                    }
                })
        }

        // first add the query in the full list
        val fullListRef =  popularSearchesDbReference.child("fullList").child(query)
        fullListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentCount = dataSnapshot.child("count").getValue(Long::class.java) ?: 0
                    fullListRef.child("count").setValue(currentCount + 1)
                    fullListRef.child("searchString").setValue(query)

                    //check if the query count makes it to the top 10
                    updateTopSearchList(query, currentCount + 1)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "Update Popular Searches: onCancelled", databaseError.toException())
                }
            })
    }

    private fun updateTopSearchList(query: String, newCount: Long) {
        val topSearchRef = popularSearchesDbReference.child("topList")
        topSearchRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val topList = dataSnapshot.children.mapNotNull {
                    it.child("count").getValue(Long::class.java)?.let{count ->
                        Pair(it.key?: "", count)
                    }
                }

                // Find the lowest count in the current top list
                val minEntry = topList.minByOrNull { it.second }
                val minCount = minEntry?.second ?: Long.MAX_VALUE

                // If newCount is greater than the minimum count or the list is not yet 20, add/update the entry
                if (newCount > minCount || topList.size < 10) {
                    if (topList.size >= 10 && minEntry != null) {
                        topSearchRef.child(minEntry.first).removeValue()
                    }
                    topSearchRef.child(query).child("searchString").setValue(query)
                    topSearchRef.child(query).child("count").setValue(newCount)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Update Top Search List: onCancelled", databaseError.toException())
            }
        })
    }


    fun setIsSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
    }

    fun updateRecentSearches(newRecentSearches: List<String>) {
        _recentSearches.value = newRecentSearches
    }

}
