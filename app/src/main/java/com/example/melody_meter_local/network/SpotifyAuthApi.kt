package com.example.melody_meter_local.network

import com.example.melody_meter_local.model.SpotifyTokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

// define endpoint to obtain access token
interface SpotifyAuthApi {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("token")
    @FormUrlEncoded
    suspend fun getToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<SpotifyTokenResponse>
}
