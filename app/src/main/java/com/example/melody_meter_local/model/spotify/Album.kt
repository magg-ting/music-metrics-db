package com.example.melody_meter_local.model.spotify

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class Album(
    @SerializedName("id") val spotifyAlbumId: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<AlbumImage>? = null,
    @SerializedName("artists") val artists: List<Artist>
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        TODO("images"),
        TODO("artists")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(spotifyAlbumId)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}
