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
        // Fetch all popular searches from Firebase Realtime Database
        Log.d("TrendingRepository", "Fetching popular searches from Firebase Realtime Database")
        return try {
            val snapshot =
                popularSearchesDbReference.orderByChild("count").limitToLast(10).get().await()
            val popularSearches = snapshot.children.mapNotNull {
                it.child("searchString").getValue(String::class.java)
            }
            Log.d("TrendingRepository", "Fetched popular searches: $popularSearches")
            popularSearches.reversed() // since Firebase returns the smallest first, reverse to get the largest count first
        } catch (e: Exception) {
            Log.e("TrendingRepository", "Error fetching popular searches", e)
            emptyList()
        }
    }

    suspend fun fetchFirstSongResult(search: String): Track? {
        // Fetch the first song result for a given search term from Spotify API
        Log.d("TrendingRepository", "Fetching first song result for search: $search")
        return try {
            val response = spotifyApi.search(query = search)
            if (response.isSuccessful) {
                val track = response.body()?.tracks?.items?.firstOrNull()
                Log.d("TrendingRepository", "Fetched track: $track")
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
        Log.d("TrendingRepository", "Fetching trending songs")
        val topSearches: List<String> = fetchPopularSearches()
        Log.d("TrendingRepository", "Top searches: $topSearches")

        val trendingSongs = mutableListOf<Song>()
        for (search in topSearches) {
            val song = fetchFirstSongResult(search)?.toSong()
            if (song != null) {
                trendingSongs.add(song)
            }
        }
        Log.d("TrendingRepository", "Trending songs: $trendingSongs")
        return trendingSongs
    }
}