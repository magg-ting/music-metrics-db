package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.network.SpotifyApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoritesRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    private val api: SpotifyApi
) {

    suspend fun fetchFavoriteSongs(): List<Song> {
        val uid = firebaseAuth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = userDbReference.child(uid).child("favorites").get().await()
            val songIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            fetchSongsDetails(songIds)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Failed to fetch favorites", e)
            emptyList()
        }
    }

    suspend fun fetchSongsDetails(songIds: List<String>): List<Song> {
        return try {
            songIds.mapNotNull { songId ->
                val response = api.getSongById(songId)
                if (response.isSuccessful) {
                    response.body()?.toSong()
                } else {
                    Log.e(
                        "FavoritesRepository",
                        "Failed to fetch song details: ${response.message()}"
                    )
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error fetching song details", e)
            emptyList()
        }
    }

    suspend fun addFavorite(songId: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        userDbReference.child(uid).child("favorites").push().setValue(songId).await()
    }

    suspend fun removeFavorite(songId: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val snapshot = userDbReference.child(uid).child("favorites").get().await()
        val children = snapshot.children.toList()
        for (child in children) {
            if (child.getValue(String::class.java) == songId) {
                child.ref.removeValue().await()
                break
            }
        }
    }
}