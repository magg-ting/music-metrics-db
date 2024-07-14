package com.example.melody_meter_local.model

data class User(
    var username: String = "",
    var profileUrl: String? = null,
    var ratings: List<Double>? = null,
    var recentSearches: List<String>? = null,
    var favorites: List<String>? = null
)