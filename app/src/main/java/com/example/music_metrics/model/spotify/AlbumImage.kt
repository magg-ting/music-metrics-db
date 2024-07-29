package com.example.music_metrics.model.spotify

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AlbumImage(
    @SerializedName("url") val url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlbumImage> {
        override fun createFromParcel(parcel: Parcel): AlbumImage {
            return AlbumImage(parcel)
        }

        override fun newArray(size: Int): Array<AlbumImage?> {
            return arrayOfNulls(size)
        }
    }
}