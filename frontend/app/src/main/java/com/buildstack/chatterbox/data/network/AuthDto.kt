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
    val profilePic: String? = "",
    val isOnline: Boolean? = false,
    val lastOnline: String? = null
)

data class ChatDto(
    val _id: String,
    val chatName: String? = null,
    val isGroupChat: Boolean? = false,
    val participants: List<UserDto>,
    @SerializedName("lastMessage") val latestMessage: MessageDto?
)

data class MessageDto(
    val _id: String,
    val sender: UserDto,
    @SerializedName("text") val content: String,
    @SerializedName("chatId") val chatElement: com.google.gson.JsonElement?,
    val imageUrl: String? = null,
    val replyTo: MessageDto? = null,
    val createdAt: String
) {
    val chat: ChatDto?
        get() = try {
            if (chatElement?.isJsonObject == true) {
                com.google.gson.Gson().fromJson(chatElement, ChatDto::class.java)
            } else null
        } catch (e: Exception) { null }

    val chatIdString: String?
        get() {
            if (chatElement == null) return null
            if (chatElement.isJsonPrimitive == true) return chatElement.asString
            if (chatElement.isJsonObject == true) {
                try {
                    return chatElement.asJsonObject.get("_id")?.asString
                } catch (e: Exception) { return null }
            }
            return null
        }
}

data class SendMessageRequest(
    val chatId: String,
    val content: String,
    val imageUrl: String? = null,
    val replyTo: String? = null
)

data class BlockUserRequest(
    val userIdToBlock: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
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
