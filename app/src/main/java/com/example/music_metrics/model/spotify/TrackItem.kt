package com.example.music_metrics.model.spotify

data class TrackItem(
    val name: String,
    val artists: List<Artist>,
    val id: String
) {
    fun toTrack(album: Album): Track {
        return Track(
            name = this.name,
            artists = this.artists,
            album = album,
            id = this.id
        )
    }
}

