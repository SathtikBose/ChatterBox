package com.buildstack.chatterbox.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object ForgotPasswordSuccess : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Auto-login check
        val savedToken = tokenManager.getToken()
        if (!savedToken.isNullOrEmpty()) {
            _authState.value = AuthState.Success(savedToken)
        }
    }

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.login(LoginRequest(email, passwordHash))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    _authState.value = AuthState.Success(token)
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun register(email: String, username: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.register(RegisterRequest(email, username, passwordHash))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    _authState.value = AuthState.Success(token)
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _authState.value = AuthState.ForgotPasswordSuccess
                } else {
                    _authState.value = AuthState.Error("Failed to send OTP: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Idle
    }
}
