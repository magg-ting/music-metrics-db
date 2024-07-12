package com.example.melody_meter_local.model.spotify

import com.example.melody_meter_local.model.spotify.Artists
import com.example.melody_meter_local.model.spotify.Tracks

data class SpotifyResponse(
    val tracks: Tracks?,
    val artists: Artists?
)