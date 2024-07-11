package com.example.melody_meter_local.data

data class Song (
    val spotifyId: String = "",
    val name: String = "",
    val artist: String = "",
    val imgUrl: String? = null,
    var avgRating: Double = 0.0
)