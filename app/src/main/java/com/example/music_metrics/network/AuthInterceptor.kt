package com.example.music_metrics.network


import com.example.music_metrics.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

// intercept HTTP requests to add the access token
class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var accessToken = runBlocking { authRepository.getAccessToken() }

        val response = chain.proceed(newRequestWithAccessToken(accessToken, request))

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            accessToken = runBlocking { authRepository.refreshAccessToken() }
            if (accessToken.isNullOrBlank()) {
                runBlocking { authRepository.getSessionManager().clearTokens() }
                return response
            }
            return chain.proceed(newRequestWithAccessToken(accessToken, request))
        }

        return response
    }

    private fun newRequestWithAccessToken(accessToken: String?, request: Request): Request =
        request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
}

