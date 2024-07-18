package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.model.Song
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class TopRatedRepository {
    private val songDbReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Songs")

    suspend fun fetchTopRatedSongs(): List<Song> {
        return try {
            val snapshot = songDbReference.orderByChild("avgRating").limitToLast(10).get().await()
            val songs = mutableListOf<Song>()
            for (songSnapshot in snapshot.children) {
                val song = songSnapshot.getValue(Song::class.java)
                song?.let { songs.add(it) }
            }
            songs
        } catch (e: Exception) {
            Log.e("TopRatedRepository", "Error fetching top-rated songs", e)
            emptyList()
        }
    }
}