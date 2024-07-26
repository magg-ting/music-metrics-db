package com.example.melody_meter_local.repository

import com.example.melody_meter_local.di.UserDatabaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecentSearchesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference
) {
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
}