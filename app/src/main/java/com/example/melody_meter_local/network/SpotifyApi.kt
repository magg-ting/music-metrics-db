package com.example.melody_meter_local.network

import com.example.melody_meter_local.model.spotify.SpotifyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// define search endpoint
interface SpotifyApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "track,artist"  //search both tracks and artists
    ): Response<SpotifyResponse>
}