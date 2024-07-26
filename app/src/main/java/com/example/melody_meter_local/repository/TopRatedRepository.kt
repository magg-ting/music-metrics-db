package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.model.Song
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TopRatedRepository @Inject constructor(
    @SongDatabaseReference var songDbReference: DatabaseReference
) {

    suspend fun fetchTopRatedSongs(): List<Song> {
        Log.d("TopRatedRepository", "Fetching top-rated songs")
        return try {
            //Query the db by descending avgRating to fetch the top 20 highest rated songs
            val snapshot = songDbReference.orderByChild("avgRating").limitToLast(20).get().await()
            val songs = mutableListOf<Song>()
            for (songSnapshot in snapshot.children) {
                val song = songSnapshot.getValue(Song::class.java)
                song?.let { songs.add(it) }
            }
            // since Firebase returns the smallest first, reverse to get the largest rating first
            songs.reversed()

        } catch (e: Exception) {
            Log.e("TopRatedRepository", "Error fetching top-rated songs", e)
            emptyList()
        }
    }
}