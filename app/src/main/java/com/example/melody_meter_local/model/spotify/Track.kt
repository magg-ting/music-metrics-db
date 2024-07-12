package com.example.melody_meter_local.model.spotify

data class Track(
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val id: String
)
