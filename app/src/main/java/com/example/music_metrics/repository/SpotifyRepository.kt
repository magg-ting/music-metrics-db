package com.example.music_metrics.repository

import android.util.Log
import com.example.music_metrics.di.SongDatabaseReference
import com.example.music_metrics.model.Song
import com.example.music_metrics.network.SpotifyApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpotifyRepository @Inject constructor(
    private val api: SpotifyApi,
    @SongDatabaseReference private val songDbReference: DatabaseReference
) {
    // search songs from api and fetch their current ratings from db
    suspend fun search(query: String): List<Song>{
        val response = api.search(query)
        if (response.isSuccessful) {
            val spotifyResponse = response.body()
            return spotifyResponse?.let { res ->
                val tracks = res.tracks?.items ?: emptyList()
                val songs = tracks.map { it.toSong() }
                fetchSongRatings(songs)
            }?: emptyList()
        } else {
            throw Exception("Search failed with response: ${response.errorBody()?.string()}")
        }
    }

    suspend fun getNewReleases() = api.getNewReleases()

    suspend fun getAlbumTracks(albumId: String) = api.getAlbumTracks(albumId)

    // get song ratings and avgRating by songId
    suspend fun getSongRatings(songId: String): Pair<MutableList<MutableMap<String, Double>>, Double> {
        val songRef = songDbReference.child(songId)

        val ratingsSnapshot = songRef.child("ratings").get().await()
        val songRatings = ratingsSnapshot.getValue(object :
            GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()

        val avgRatingSnapshot = songRef.child("avgRating").get().await()
        val avgRating = (avgRatingSnapshot.value as Number).toDouble()

        return Pair(songRatings, avgRating)
    }

    // get the ratings of a list of songs
    suspend fun fetchSongRatings(songs: List<Song>): List<Song> {
        return withContext(Dispatchers.IO) {
            songs.map { song ->
                try {
                    val (songRatings, avgRating) = getSongRatings(song.spotifyTrackId)
                    song.copy(
                        ratings = songRatings,
                        avgRating = avgRating
                    )
                } catch (e: Exception) {
                    Log.d("SpotifyRepository", "Failed to fetch ratings for song: ${song.spotifyTrackId}", e)
                    song.copy(ratings = mutableListOf(), avgRating = 0.0)
                }
            }
        }
    }
}
