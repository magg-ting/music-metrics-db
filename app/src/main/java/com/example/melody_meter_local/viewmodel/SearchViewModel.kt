package com.example.melody_meter_local.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melody_meter_local.di.PopularSearchesDatabaseReference
import com.example.melody_meter_local.di.UserDatabaseReference
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @PopularSearchesDatabaseReference private val popularSearchesDbReference: DatabaseReference,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> get() = _searchResults

    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> get() = _isSearching

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> get() = _recentSearches

    fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    spotifyRepository.search(query)
                }
                if (response.isSuccessful) {
                    val spotifyResponse = response.body()
                    spotifyResponse?.let { res ->
                        val results = res.tracks?.items?.map { it.toSong() } ?: emptyList()
                        _searchResults.postValue(results)
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

    fun setIsSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
    }

    fun updateRecentSearches(newRecentSearches: List<String>) {
        _recentSearches.value = newRecentSearches
    }

    fun addSearchQuery(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: mutableListOf()
        if (query !in currentSearches) {
            currentSearches.add(0, query)
            if (currentSearches.size > 10) {
                currentSearches.removeAt(currentSearches.size - 1)
            }
            updateRecentSearches(currentSearches)
        }
    }

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

        popularSearchesDbReference.child(query)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentCount = dataSnapshot.child("count").getValue(Long::class.java) ?: 0
                    popularSearchesDbReference.child(query).child("count").setValue(currentCount + 1)
                    popularSearchesDbReference.child(query).child("searchString").setValue(query)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "Update Popular Searches: onCancelled", databaseError.toException())
                }
            })
    }

    fun loadRecentSearches() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = userDbReference.child(uid)
        userRef.child("recentSearches")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recentSearches = dataSnapshot.getValue(object :
                        GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                    _recentSearches.postValue(recentSearches)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "Load Recent Searches: onCancelled", databaseError.toException())
                }
            })
    }

}
