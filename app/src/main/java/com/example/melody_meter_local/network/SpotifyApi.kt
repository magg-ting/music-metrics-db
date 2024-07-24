package com.example.melody_meter_local.network

import com.example.melody_meter_local.model.spotify.AlbumTracksResponse
import com.example.melody_meter_local.model.spotify.NewReleasesResponse
import com.example.melody_meter_local.model.spotify.SearchResponse
import com.example.melody_meter_local.model.spotify.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// define search endpoint
interface SpotifyApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "track,artist"  //search both tracks and artists
    ): Response<SearchResponse>

    @GET("browse/new-releases")
    suspend fun getNewReleases(): Response<NewReleasesResponse>

    @GET("tracks/{id}")
    suspend fun getSongById(
        @Path("id") id: String
    ): Response<Track>

    @GET("albums/{id}/tracks")
    suspend fun getAlbumTracks(
        @Path("id") id: String
    ): Response<AlbumTracksResponse>
}