package com.buildstack.chatterbox.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.TokenManager
import com.buildstack.chatterbox.data.network.UpdateProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)
    
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _username = MutableStateFlow(tokenManager.getUsername() ?: "")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow(tokenManager.getEmail() ?: "")
    val email: StateFlow<String> = _email.asStateFlow()

    fun updateProfile(newUsername: String, profilePicUrl: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = apiService.updateProfile(UpdateProfileRequest(newUsername, profilePicUrl))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _username.value = user.username
                    
                    // Update cache
                    tokenManager.saveToken(
                        tokenManager.getToken() ?: "",
                        user._id,
                        user.username,
                        user.email,
                        user.profilePic
                    )
                    
                    _profileState.value = ProfileState.Success("Profile updated successfully")
                } else {
                    _profileState.value = ProfileState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _profileState.value = ProfileState.Idle
    }
}
