package com.buildstack.chatterbox.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<GenericResponse>

    // Users
    @GET("/api/users")
    suspend fun searchUsers(@Query("search") search: String): Response<List<UserDto>>

    @PUT("/api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    @POST("/api/user/block")
    suspend fun blockUser(@Body request: BlockUserRequest): Response<GenericResponse>

    @PUT("/api/user/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<GenericResponse>

    @Multipart
    @POST("/api/upload")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): Response<UploadResponse>

    // Chats
    @POST("/api/chats")
    suspend fun accessChat(@Body request: AccessChatRequest): Response<ChatDto>

    @GET("/api/chats")
    suspend fun fetchChats(): Response<List<ChatDto>>

    // Messages
    @GET("/api/chats/message/{chatId}")
    suspend fun allMessages(@Path("chatId") chatId: String): Response<List<MessageDto>>

    @POST("/api/chats/message")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageDto>
}
