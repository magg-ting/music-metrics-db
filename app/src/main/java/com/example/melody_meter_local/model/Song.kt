package com.example.melody_meter_local.model

import android.os.Parcel
import android.os.Parcelable


data class Song(
    val spotifyTrackId: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String = "",
    val imgUrl: String? = null,
    var ratings: MutableList<MutableMap<String, Double>> = mutableListOf(),
    var avgRating: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        // Deserialize ratings
        mutableListOf<MutableMap<String, Double>>().apply {
            val size = parcel.readInt()
            repeat(size) {
                val map = mutableMapOf<String, Double>()
                val mapSize = parcel.readInt()
                repeat(mapSize) {
                    val key = parcel.readString().orEmpty()
                    val value = parcel.readDouble()
                    map[key] = value
                }
                add(map)
            }
        },
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(spotifyTrackId)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(imgUrl)
        // Serialize ratings
        parcel.writeInt(ratings.size)
        ratings.forEach { map ->
            parcel.writeInt(map.size)
            map.forEach { (key, value) ->
                parcel.writeString(key)
                parcel.writeDouble(value)
            }
        }
        parcel.writeDouble(avgRating)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}