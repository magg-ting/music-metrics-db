package com.example.melody_meter_local.data

data class User(
    var username: String = "",
    var profileUrl: String? = null,
    var rating: List<Double>? = null,
    var recentSearches: List<String>? = null
)