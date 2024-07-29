package com.example.music_metrics.repository

import com.example.music_metrics.BuildConfig
import com.example.music_metrics.network.SessionManager
import com.example.music_metrics.network.SpotifyAuthApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// this class handles the logic for obtaining and refreshing the access token
class AuthRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val authApi: SpotifyAuthApi
) {
    suspend fun getAccessToken(): String? {
        return sessionManager.getAccessToken() ?: refreshAccessToken()
    }

    suspend fun refreshAccessToken(): String? {
        val response = authApi.getToken(
            clientId = BuildConfig.SPOTIFY_CLIENT_ID,
            clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET,
            grantType = "client_credentials"
        )

        return if (response.isSuccessful) {
            response.body()?.let {
                sessionManager.saveTokens(it.accessToken, it.expiresIn)
                it.accessToken
            }
        } else {
            sessionManager.clearTokens()
            null
        }
    }

    fun getSessionManager(): SessionManager {
        return sessionManager
    }
}