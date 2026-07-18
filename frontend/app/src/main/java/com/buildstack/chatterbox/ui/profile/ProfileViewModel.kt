package com.buildstack.chatterbox.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.TokenManager
import com.buildstack.chatterbox.data.network.UpdateProfileRequest
import com.buildstack.chatterbox.data.network.ChangePasswordRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
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

    private val _profilePic = MutableStateFlow(tokenManager.getProfilePic() ?: "")
    val profilePic: StateFlow<String> = _profilePic.asStateFlow()

    fun updateProfile(newUsername: String, profilePicUrl: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = apiService.updateProfile(UpdateProfileRequest(newUsername, profilePicUrl))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _username.value = user.username
                    if (!user.profilePic.isNullOrEmpty()) {
                        _profilePic.value = user.profilePic
                    }
                    
                    // Update cache
                    tokenManager.saveToken(
                        tokenManager.getToken() ?: "",
                        user._id,
                        user.username,
                        user.email,
                        user.profilePic ?: ""
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

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = apiService.changePassword(ChangePasswordRequest(oldPass, newPass))
                if (response.isSuccessful) {
                    _profileState.value = ProfileState.Success("Password changed successfully")
                } else {
                    _profileState.value = ProfileState.Error("Failed to change password")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun uploadProfileImage(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = java.io.File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }
                
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestFile = tempFile.asRequestBody(mediaType)
                val body = okhttp3.MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                
                val uploadResponse = apiService.uploadImage(body)
                if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
                    val imageUrl = uploadResponse.body()!!.imageUrl
                    updateProfile(_username.value, imageUrl)
                } else {
                    _profileState.value = ProfileState.Error("Failed to upload image")
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
