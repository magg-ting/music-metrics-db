package com.example.melody_meter_local.repository

import com.example.melody_meter_local.network.SpotifyApi
import javax.inject.Inject

class SpotifyRepository @Inject constructor(
    private val api: SpotifyApi
){
    suspend fun search(query: String) = api.search(query)
}
