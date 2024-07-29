package com.example.melody_meter_local.repository

import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.network.SpotifyApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SpotifyRepository @Inject constructor(
    private val api: SpotifyApi,
    @SongDatabaseReference private val songDbReference: DatabaseReference
) {
    suspend fun search(query: String) = api.search(query)

    suspend fun getNewReleases() = api.getNewReleases()

    suspend fun getAlbumTracks(albumId: String) = api.getAlbumTracks(albumId)

    suspend fun getSongRatings(songId: String): Pair<MutableList<MutableMap<String, Double>>, Double> {
        val songRef = songDbReference.child(songId)

        val ratingsSnapshot = songRef.child("ratings").get().await()
        val songRatings = ratingsSnapshot.getValue(object :
            GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {}) ?: mutableListOf()

        val avgRatingSnapshot = songRef.child("avgRating").get().await()
        val avgRating = (avgRatingSnapshot.value as Number).toDouble()

        return Pair(songRatings, avgRating)
    }
}
