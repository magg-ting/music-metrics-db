package com.example.music_metrics.model.spotify

import com.example.music_metrics.model.Song

data class Track(
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val id: String
) {
    fun toSong(): Song {
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
}
