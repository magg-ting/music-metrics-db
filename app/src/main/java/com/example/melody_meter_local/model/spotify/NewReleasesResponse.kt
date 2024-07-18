package com.example.melody_meter_local.model.spotify

data class NewReleasesResponse(
    val albums: Albums
)

data class Albums(
    val items: List<Album>
)
