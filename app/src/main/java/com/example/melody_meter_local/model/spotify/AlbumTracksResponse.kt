package com.example.melody_meter_local.model.spotify

import com.example.melody_meter_local.model.Song

data class AlbumTracksResponse(
    val items: List<TrackItem>
) {
    fun toSongs(album: Album): List<Song> {
        return items.map { it.toTrack(album).toSong() }
    }
}