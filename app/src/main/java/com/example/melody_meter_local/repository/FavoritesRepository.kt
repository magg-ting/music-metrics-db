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

class FavoritesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference,
    @SongDatabaseReference private val songDbReference: DatabaseReference
) {

    suspend fun fetchFavoriteSongs(): List<Song> {
        val uid = auth.currentUser?.uid ?: return emptyList()

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
                }
            }
            songs
        }

        catch (e: Exception) {
            Log.e("FavoritesRepository", "Error fetching song details", e)
            emptyList()
        }
    }

    suspend fun addFavorite(songId: String) {
        val uid = auth.currentUser?.uid ?: return
        userDbReference.child(uid).child("favorites").push().setValue(songId).await()
    }

    suspend fun removeFavorite(songId: String) {
        val uid = auth.currentUser?.uid ?: return
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