package com.example.navapp.data.connection

import com.example.navapp.data.models.AuthResponse
import com.example.navapp.data.models.LoginRequest
import com.example.navapp.data.models.RegisterRequest
import com.example.navapp.data.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}

interface ApiService {
    @GET("users/user")
    suspend fun getUser(
        @Query("userId") userId: String? = null
    ): Response<UserResponse>
}