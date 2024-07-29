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

class FavoritesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @SongDatabaseReference private val songDbReference: DatabaseReference
) {

    suspend fun fetchFavoriteSongs(): List<Song> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = userDbReference.child(uid).child("favorites").get().await()

            // Map the favorite entries to their keys and sort by key in descending order
            val sortedSongIds = snapshot.children
                .mapNotNull { child ->
                    val key = child.key?.toIntOrNull()
                    val songId = child.getValue(String::class.java)
                    key to songId
                }
                .sortedByDescending { it.first }
                .mapNotNull { it.second }
            fetchSongsDetails(sortedSongIds)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Failed to fetch favorites", e)
            emptyList()
        }
    }

    suspend fun fetchSongsDetails(songIds: List<String>): List<Song> {
        return try {
            val songs = mutableListOf<Song>()
            songIds.forEach { songId ->
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

                    val avgRating =
                        (snapshot.child("avgRating").value as? Number)?.toDouble() ?: 0.0
                    song.avgRating = avgRating
                    songs.add(song)
                } else {
                    Log.d("FavoritesRepository", "No snapshot exists for this songId")
                }
            }
            songs
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error fetching song details", e)
            emptyList()
        }
    }

    suspend fun addFavorite(songId: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val userFavoritesRef = userDbReference.child(uid).child("favorites")
            val snapshot = userFavoritesRef.get().await()
            val favorites = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

            if (!favorites.contains(songId)) {
                favorites.add(songId)
                userFavoritesRef.setValue(favorites).await()
            }
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Failed to add favorite", e)
        }
    }

    suspend fun removeFavorite(songId: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val userFavoritesRef = userDbReference.child(uid).child("favorites")
            val snapshot = userFavoritesRef.get().await()
            val favorites = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

            if (favorites.contains(songId)) {
                favorites.remove(songId)
                userFavoritesRef.setValue(favorites).await()
            }
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Failed to remove favorite", e)
        }
    }
}