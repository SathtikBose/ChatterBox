package com.buildstack.chatterbox.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<GenericResponse>

    // Users
    @GET("/api/users")
    suspend fun searchUsers(@retrofit2.http.Query("search") search: String): Response<List<UserDto>>

    @retrofit2.http.PUT("/api/users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    // Chats
    @POST("/api/chats")
    suspend fun accessChat(@Body request: AccessChatRequest): Response<ChatDto>

    @GET("/api/chats")
    suspend fun fetchChats(): Response<List<ChatDto>>

    // Messages
    @GET("/api/chats/{chatId}")
    suspend fun allMessages(@retrofit2.http.Path("chatId") chatId: String): Response<List<MessageDto>>

    @POST("/api/chats/message")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageDto>

    // Upload
    @retrofit2.http.Multipart
    @POST("/api/upload")
    suspend fun uploadImage(@retrofit2.http.Part image: okhttp3.MultipartBody.Part): Response<UploadResponse>
}
