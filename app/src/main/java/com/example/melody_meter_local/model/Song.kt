package com.example.melody_meter_local.model

data class Song (
    val spotifyTrackId: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String? = null,
    val imgUrl: String? = null,
    var avgRating: Double = 0.0
)