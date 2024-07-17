package com.example.melody_meter_local.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class Song (
    val spotifyTrackId: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String? = null,
    val imgUrl: String? = null,
    var ratings: List<Double> = listOf()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        TODO("ratings")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(spotifyTrackId)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(imgUrl)
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