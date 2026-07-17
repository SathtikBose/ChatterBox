package com.buildstack.chatterbox.data.network

data class RegisterRequest(
    val email: String,
    val username: String,
    val passwordHash: String
)

data class LoginRequest(
    val email: String,
    val passwordHash: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class AuthResponse(
    val token: String,
    val message: String
)

data class GenericResponse(
    val message: String
)
