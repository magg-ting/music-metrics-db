package com.example.melody_meter_local.utils

import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.model.spotify.Track

// Mapping function to convert Track to Song
fun Track.toSong(): Song {
    val artistNames = this.artists.joinToString(", ") { it.name }
    val albumImageUrl = this.album.images?.firstOrNull()?.url

    return Song(
        spotifyTrackId = this.id,
        name = this.name,
        artist = artistNames,
        album = this.album.name,
        imgUrl = albumImageUrl
    )
}