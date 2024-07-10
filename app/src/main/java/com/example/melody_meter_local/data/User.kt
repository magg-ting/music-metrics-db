package com.example.melody_meter_local.data

data class User(
    var username : String? = null,
    val email : String = "",
    var password : String = "",
    var profileUrl : String = "",
    var rating : List<String>? = null,
    var isLoggedIn : Boolean = false
)
