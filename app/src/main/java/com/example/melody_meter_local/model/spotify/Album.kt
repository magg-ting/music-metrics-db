package com.example.melody_meter_local.model.spotify

data class Album(
    val spotifyAlbumId: String,
    val name: String,
    val images: List<AlbumImage>? = null,
    val artists: List<Artist>
)
