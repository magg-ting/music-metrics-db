package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.di.PopularSearchesDatabaseReference
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.spotify.Track
import com.example.melody_meter_local.network.SpotifyApi
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TrendingRepository @Inject constructor(
    private val spotifyApi: SpotifyApi,
    @PopularSearchesDatabaseReference private val popularSearchesDbReference: DatabaseReference
) {

    suspend fun fetchPopularSearches(): List<String> {
        // Fetch top 20 popular searches from Firebase
        return try {
            val snapshot =
                popularSearchesDbReference.orderByChild("count").limitToFirst(20).get().await()
            val popularSearches = snapshot.children.mapNotNull {
                it.child("searchString").getValue(String::class.java)
            }
            // since Firebase returns the smallest first, reverse to get the largest count first
            popularSearches.reversed()
        } catch (e: Exception) {
            Log.e("TrendingRepository", "Error fetching popular searches", e)
            emptyList()
        }
    }

    suspend fun fetchFirstSongResult(search: String): Track? {
        // Fetch the first song result for a given search term from Spotify API
        return try {
            val response = spotifyApi.search(query = search)
            if (response.isSuccessful) {
                val track = response.body()?.tracks?.items?.firstOrNull()
                track
            } else {
                Log.e(
                    "TrendingRepository",
                    "Spotify API response unsuccessful: ${response.message()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("TrendingRepository", "Error fetching song result from Spotify API", e)
            null
        }
    }

    suspend fun fetchTrendingSongs(): List<Song> {
        val topSearches: List<String> = fetchPopularSearches()
        val trendingSongs = mutableListOf<Song>()
        for (search in topSearches) {
            val song = fetchFirstSongResult(search)?.toSong()
            if (song != null) {
                trendingSongs.add(song)
            }
        }
        return trendingSongs
    }
}