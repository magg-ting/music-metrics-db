package com.example.melody_meter_local.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    var username: String = "",
    var profileUrl: String? = null,
    var ratings: List<MutableMap<String, Double>>? = null,
    var recentSearches: List<String>? = null,
    var favorites: List<String>? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        TODO("ratings"),
        parcel.createStringArrayList(),
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(profileUrl)
        parcel.writeStringList(recentSearches)
        parcel.writeStringList(favorites)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}