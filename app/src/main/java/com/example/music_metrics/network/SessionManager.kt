package com.example.music_metrics.network

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

// manage access token storage in the current session
@Singleton
class SessionManager @Inject constructor(
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("spotify_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, expiresIn: Long) {
        val editor = prefs.edit()
        editor.putString("access_token", accessToken)
        editor.putLong("expires_in", System.currentTimeMillis() + expiresIn * 1000)
        editor.apply()
    }

    fun getAccessToken(): String? {
        val expiresIn = prefs.getLong("expires_in", 0)
        return if (System.currentTimeMillis() < expiresIn) {
            prefs.getString("access_token", null)
        } else {
            null
        }
    }

    fun clearTokens() {
        val editor = prefs.edit()
        editor.remove("access_token")
        editor.remove("expires_in")
        editor.apply()
    }
}