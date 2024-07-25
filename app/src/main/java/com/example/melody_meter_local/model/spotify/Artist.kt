package com.example.melody_meter_local.model.spotify

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: String
)

