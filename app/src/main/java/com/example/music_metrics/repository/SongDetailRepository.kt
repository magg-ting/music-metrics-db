package com.example.music_metrics.repository

import android.util.Log
import com.example.music_metrics.di.SongDatabaseReference
import com.example.music_metrics.di.UserDatabaseReference
import com.example.music_metrics.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SongDetailRepository @Inject constructor(
    @SongDatabaseReference private val songDbReference: DatabaseReference,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    private val auth: FirebaseAuth
){
    suspend fun getSongDetails(spotifyTrackId: String): Song? {
        return try {
            val songRef = songDbReference.child(spotifyTrackId).get().await()
            songRef.getValue(Song::class.java)
        } catch (e: Exception) {
            Log.e("SongDetailRepository", "Failed to fetch song details", e)
            null
        }
    }

    suspend fun isFavorite(spotifyTrackId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userRef = userDbReference.child(uid)
            val dataSnapshot = userRef.child("favorites").get().await()
            val favorites = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
            favorites?.contains(spotifyTrackId) ?: false
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to get user favorites", e)
            false
        }
    }

    suspend fun toggleFavorite(spotifyTrackId: String, song: Song): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userRef = userDbReference.child(uid)
            val songRef = songDbReference.child(spotifyTrackId)

            val dataSnapshot = userRef.child("favorites").get().await()
            val favorites = dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

            val isFavorite = if (favorites.contains(spotifyTrackId)) {
                favorites.remove(spotifyTrackId)
                false
            } else {
                favorites.add(spotifyTrackId)

                val songSnapshot = songRef.get().await()
                if (!songSnapshot.exists()) {
                    songRef.setValue(song).await()
                }
                true
            }

            userRef.child("favorites").setValue(favorites).await()
            isFavorite
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to update favorites", e)
            false
        }
    }

    suspend fun saveRating(song: Song, rating: Double): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userRef = userDbReference.child(uid)
            val songRef = songDbReference.child(song.spotifyTrackId)

            // Update the song DB
            val songDataSnapshot = songRef.child("ratings").get().await()
            val songRatings = songDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()

            // Check if user already rated the song before
            val existingRatingByUser = songRatings.find { it.contains(uid) }
            if (existingRatingByUser != null) {
                // Update existing rating
                existingRatingByUser[uid] = rating
            } else {
                // Add new rating
                songRatings.add(mutableMapOf(uid to rating))
            }

            // Update average rating of the song
            val average = if (songRatings.isNotEmpty()) {
                songRatings.sumOf { it.values.first() } / songRatings.size
            } else {
                0.0
            }

            // Update song details along with the ratings and average rating
            songRef.setValue(
                mapOf(
                    "spotifyTrackId" to song.spotifyTrackId,
                    "name" to song.name,
                    "artist" to song.artist,
                    "album" to song.album,
                    "imgUrl" to song.imgUrl,
                    "ratings" to songRatings,
                    "avgRating" to average
                )
            ).await()


            // Update the user DB
            val userDataSnapshot = userRef.child("ratings").get().await()
            val userRatings = userDataSnapshot.getValue(object : GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()

            // Check if user already rated the song before
            val existingRatingOfSong = userRatings.find { it.contains(song.spotifyTrackId) }
            if (existingRatingOfSong != null) {
                // Update existing rating
                existingRatingOfSong[song.spotifyTrackId] = rating
            } else {
                // Add new rating
                userRatings.add(mutableMapOf(song.spotifyTrackId to rating))
            }

            userRef.child("ratings").setValue(userRatings).await()
            true
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to save rating", e)
            false
        }
    }

}