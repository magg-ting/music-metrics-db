package com.example.melody_meter_local.repository

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
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @SongDatabaseReference private val songDbReference: DatabaseReference
) {

    //TODO: order by latest to oldest?
    suspend fun fetchRatingHistory(): List<Pair<Song, Double>> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = userDbReference.child(uid).child("ratings").get().await()
            val ratedSongs = mutableListOf<Pair<Song, Double>>()

            snapshot.children.forEach { ratingSnapshot ->
                Log.d("RatingHistoryRepository", "Rating entry: ${ratingSnapshot.value}")
                val ratingMap = ratingSnapshot.value as? Map<String, Any> ?: return@forEach

                // Process each song rating in the current snapshot
                ratingMap.forEach { (songId, rating) ->
                    val ratingValue = when (rating) {
                        is Number -> rating.toDouble()
                        else -> null
                    }

                    if (ratingValue != null) {
                        val song = fetchSongsDetails(songId)
                        if (song != null) {
                            ratedSongs.add(Pair(song, ratingValue))
                        } else {
                            Log.w("RatingHistoryRepository", "Failed to fetch song details for songId: $songId")
                        }
                    }
                }
            }
            ratedSongs
        } catch (e: Exception) {
            Log.e("RatingHistoryRepository", "Failed to fetch rating history", e)
            emptyList()
        }
    }

    suspend fun fetchSongsDetails(songId: String): Song? {

        return try {
            val snapshot = songDbReference.child(songId).get().await()
            if (snapshot.exists()) {
                val song = Song(
                    spotifyTrackId = songId,
                    name = snapshot.child("name").value.toString(),
                    artist = snapshot.child("artist").value.toString(),
                    album = snapshot.child("album").value.toString(),
                    imgUrl = snapshot.child("imgUrl").value.toString(),
                )

                val ratingsList = mutableListOf<MutableMap<String, Double>>()
                snapshot.child("ratings").children.forEach { ratingSnapshot ->
                    val ratingMap = ratingSnapshot.value as? Map<String, Any>
                    ratingMap?.let {
                        val uid = it.keys.first()
                        val ratingValue = when (val value = it[uid]) {
                            is Number -> value.toDouble()
                            else -> 0.0
                        }
                        ratingsList.add(mutableMapOf(uid to ratingValue))
                    }
                }
                song.ratings = ratingsList

                val avgRating = (snapshot.child("avgRating").value as? Number)?.toDouble() ?: 0.0
                song.avgRating = avgRating
                song
            }

            else {
                Log.w("RatingHistoryRepository", "No song found with songId: $songId")
                null
            }
        }

        catch (e: Exception) {
            Log.e("RatingHistoryRepository", "Failed to fetch song details", e)
            null
        }
    }

}