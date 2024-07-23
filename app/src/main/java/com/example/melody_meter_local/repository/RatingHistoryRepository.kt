package com.example.melody_meter_local.repository

import android.provider.ContactsContract.Data
import android.util.Log
import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.network.SpotifyApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RatingHistoryRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    private val api: SpotifyApi
) {

    suspend fun fetchRatingHistory(): List<Pair<Song, Double>> {
        val uid = firebaseAuth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = userDbReference.child(uid).child("ratings").get().await()
            val ratedSongs = mutableListOf<Pair<Song, Double>>()
            snapshot.children.forEach{ ratingSnapshot ->
                val ratingMap = ratingSnapshot.value as Map<String, Any>
                ratingMap.forEach { (songId, rating) ->
                    val ratingValue = when (rating) {
                        is Long -> rating.toDouble()
                        is Double -> rating
                        else -> null
                    }
                    if (ratingValue != null) {
                        val song = fetchSongsDetails(songId)
                        ratedSongs.add(Pair(song, ratingValue) as Pair<Song, Double>)
                    }
                }
            }
            ratedSongs
        }
        catch (e: Exception) {
            Log.e("RatingHistoryRepository", "Failed to fetch rating history", e)
            emptyList()
        }
    }

    suspend fun fetchSongsDetails(songId: String): Song? {
        return try {
                val response = api.getSongById(songId)
                if (response.isSuccessful) {
                    response.body()?.toSong()
                } else {
                    Log.e(
                        "RatingHistoryRepository",
                        "Failed to fetch song details: ${response.message()}"
                    )
                    null
                }

        } catch (e: Exception) {
            Log.e("RatingHistoryRepository", "Error fetching song details", e)
            null
        }
    }

}