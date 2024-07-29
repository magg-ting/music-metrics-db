package com.example.music_metrics.repository

import android.content.ContentValues
import android.util.Log
import com.example.music_metrics.di.PopularSearchesDatabaseReference
import com.example.music_metrics.di.UserDatabaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecentSearchesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @PopularSearchesDatabaseReference private val popularSearchesDbReference: DatabaseReference
) {
    suspend fun fetchRecentSearches(): List<String> {
        val uid = auth.currentUser?.uid?: return emptyList()
        return try {
            val snapshot = userDbReference.child(uid).child("recentSearches").get().await()
            snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: emptyList()
        } catch (e: Exception) {
            Log.e("RecentSearchesRepository", "Failed to fetch recent searches: ${e.message}")
            emptyList()
        }
    }

    suspend fun removeRecentSearch(searchItem: String){
        val uid = auth.currentUser?.uid ?: return
        val snapshot = userDbReference.child(uid).child("recentSearches").get().await()
        for(child in snapshot.children){
            if (child.getValue(String::class.java) == searchItem) {
                child.ref.removeValue().await()
                break
            }
        }
    }

    fun removeAllSearches(onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        val uid = auth.currentUser?.uid ?: return
        val userRef = userDbReference.child(uid).child("recentSearches")
        userRef.setValue(emptyList<String>())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun saveSearchQuery(query: String) {
        val uid = auth.currentUser?.uid ?: return
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
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "Load Recent Searches: onCancelled", databaseError.toException())
                }
            })

        // Add the query to the full list
        val fullListRef = popularSearchesDbReference.child("fullList").child(query)
        fullListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentCount = dataSnapshot.child("count").getValue(Long::class.java) ?: 0
                fullListRef.child("count").setValue(currentCount + 1)
                fullListRef.child("searchString").setValue(query)

                // Check if the query count makes it to the top 10
                updateTopSearchList(query, currentCount + 1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Update Popular Searches: onCancelled", databaseError.toException())
            }
        })
    }

    private fun updateTopSearchList(query: String, newCount: Long) {
        val topSearchRef = popularSearchesDbReference.child("topList")
        topSearchRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val topList = dataSnapshot.children.mapNotNull {
                    it.child("count").getValue(Long::class.java)?.let { count ->
                        Pair(it.key ?: "", count)
                    }
                }

                // Find the lowest count in the current top list
                val minEntry = topList.minByOrNull { it.second }
                val minCount = minEntry?.second ?: Long.MAX_VALUE

                // If newCount is greater than the minimum count or the list is not yet 10, add/update the entry
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

}