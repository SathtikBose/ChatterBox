package com.buildstack.chatterbox.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<Map<String, Any>>

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<Map<String, Any>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<Map<String, Any>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<Map<String, Any>>
}
