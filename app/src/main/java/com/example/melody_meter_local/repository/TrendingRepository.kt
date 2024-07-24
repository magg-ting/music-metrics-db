package com.example.melody_meter_local.repository

import android.util.Log
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.spotify.Track
import com.example.melody_meter_local.network.SpotifyApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TrendingRepository @Inject constructor(
    private val spotifyApi: SpotifyApi
) {

    private val userDbReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")

    suspend fun fetchAllRecentSearches(): List<String> {
        // Fetch all recent searches from the Firebase database
        Log.d("TrendingRepository", "Fetching all recent searches from Firebase")
        return try {
            val snapshot = userDbReference.get().await()
            val recentSearches = mutableListOf<String>()
            val t = object : GenericTypeIndicator<List<String>>() {}
            for (userSnapshot in snapshot.children) {
                val userRecentSearches = userSnapshot.child("recentSearches").getValue(t)
                if (userRecentSearches != null) {
                    recentSearches.addAll(userRecentSearches)
                }
            }
            Log.d("TrendingRepository", "Fetched recent searches: $recentSearches")
            recentSearches
        } catch (e: Exception) {
            Log.e("TrendingRepository", "Error fetching recent searches", e)
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
        val allRecentSearches: List<String> = fetchAllRecentSearches()
        val searchFrequencyMap: Map<String, Int> = allRecentSearches.groupingBy { it }.eachCount()
        val topSearches: List<String> = searchFrequencyMap.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }

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