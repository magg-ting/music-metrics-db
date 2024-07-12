package com.example.melody_meter_local.model

import com.google.gson.annotations.SerializedName

// models from the API response

data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class SpotifyResponse(
    val tracks: Tracks?,
    val artists: Artists?
)

data class Tracks(
    val items: List<Track>
)

data class Track(
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val id: String
)

data class Artists(
    val items: List<Artist>
)

data class Artist(
    val name: String,
    val id: String
)

data class Album(
    val name: String,
    val images: List<Image>
)

data class Image(
    val url: String
)