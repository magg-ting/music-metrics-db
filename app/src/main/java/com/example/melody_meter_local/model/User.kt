package com.example.melody_meter_local.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    var username: String = "",
    var profileUrl: String? = null,
    var ratings: MutableList<MutableMap<String, Double>>? = null,
    var recentSearches: MutableList<String>? = null,
    var favorites: MutableList<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        readMapList(parcel),
        parcel.createStringArrayList(),
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(profileUrl)
        writeMapList(parcel, ratings)
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

        private fun writeMapList(parcel: Parcel, list: List<MutableMap<String, Double>>?) {
            parcel.writeInt(list?.size ?: 0)
            list?.forEach { map ->
                parcel.writeInt(map.size)
                map.forEach { (key, value) ->
                    parcel.writeString(key)
                    parcel.writeDouble(value)
                }
            }
        }

        private fun readMapList(parcel: Parcel): MutableList<MutableMap<String, Double>> {
            val size = parcel.readInt()
            val list = mutableListOf<MutableMap<String, Double>>()
            repeat(size) {
                val mapSize = parcel.readInt()
                val map = mutableMapOf<String, Double>()
                repeat(mapSize) {
                    val key = parcel.readString() ?: ""
                    val value = parcel.readDouble()
                    map[key] = value
                }
                list.add(map)
            }
            return list
        }

    }

}