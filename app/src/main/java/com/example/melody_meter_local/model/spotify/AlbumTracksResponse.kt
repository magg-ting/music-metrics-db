package com.example.melody_meter_local.model.spotify

import com.example.melody_meter_local.model.Song
import com.google.gson.annotations.SerializedName

data class AlbumTracksResponse(
    @SerializedName("items") val items: List<TrackItem>
) {
    fun toSongs(album: Album): List<Song> {
        return items.map { it.toTrack(album).toSong() }
    }
}