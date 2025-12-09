package com.example.gametrack.data.remote.api

import com.example.gametrack.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("auth/check-username/{username}")
    suspend fun checkUsername(
        @Path("username") username: String
    ): Response<UsernameAvailable>

    @GET("auth/test")
    suspend fun testConnection(): Response<TestResponse>

}
