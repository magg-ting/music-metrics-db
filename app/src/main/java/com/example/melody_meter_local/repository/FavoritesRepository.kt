package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.model.Song
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoritesRepository {

    private val userDbReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    suspend fun fetchFavoriteSongs(uid: String): List<Song> {
        return try {
            val snapshot = userDbReference.child(uid).child("favorites").get().await()
            snapshot.children.mapNotNull { it.getValue(Song::class.java) }
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Failed to fetch favorites", e)
            emptyList()
        }
    }
}