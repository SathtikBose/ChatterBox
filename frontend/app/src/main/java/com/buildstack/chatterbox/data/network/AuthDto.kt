package com.buildstack.chatterbox.data.network

import com.google.gson.annotations.SerializedName
data class RegisterRequest(
    val email: String,
    val username: String,
    @SerializedName("password") val passwordHash: String
)

data class LoginRequest(
    val email: String,
    @SerializedName("password") val passwordHash: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class AuthResponse(
    val _id: String,
    val username: String,
    val email: String,
    val profilePic: String,
    val token: String
)

data class GenericResponse(
    val message: String
)

data class UserDto(
    val _id: String,
    val username: String,
    val email: String,
    val profilePic: String,
    val isOnline: Boolean,
    val lastOnline: String
)

data class ChatDto(
    val _id: String,
    val chatName: String,
    val isGroupChat: Boolean,
    val participants: List<UserDto>,
    val latestMessage: MessageDto?
)

data class MessageDto(
    val _id: String,
    val sender: UserDto,
    @SerializedName("text") val content: String,
    @SerializedName("chatId") val chat: ChatDto?,
    val createdAt: String
)

data class SendMessageRequest(
    val chatId: String,
    val content: String
)

data class AccessChatRequest(
    val userId: String
)

data class UpdateProfileRequest(
    val username: String,
    val profilePic: String
)

data class UploadResponse(
    val imageUrl: String
)
