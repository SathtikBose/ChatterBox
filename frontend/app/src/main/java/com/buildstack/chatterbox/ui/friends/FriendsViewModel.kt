package com.buildstack.chatterbox.ui.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.AccessChatRequest
import com.buildstack.chatterbox.data.network.ChatDto
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FriendsState {
    object Idle : FriendsState()
    object Loading : FriendsState()
    data class UsersLoaded(val users: List<UserDto>) : FriendsState()
    data class ChatsLoaded(val chats: List<ChatDto>) : FriendsState()
    data class ChatAccessed(val chatId: String) : FriendsState()
    data class Error(val message: String) : FriendsState()
}

class FriendsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.apiService

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Idle)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            fetchChats() // Reset to chats if query is empty
            return
        }
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            try {
                val response = apiService.searchUsers(query)
                if (response.isSuccessful && response.body() != null) {
                    _friendsState.value = FriendsState.UsersLoaded(response.body()!!)
                } else {
                    _friendsState.value = FriendsState.Error("Failed to search users")
                }
            } catch (e: Exception) {
                _friendsState.value = FriendsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchChats() {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            try {
                val response = apiService.fetchChats()
                if (response.isSuccessful && response.body() != null) {
                    _friendsState.value = FriendsState.ChatsLoaded(response.body()!!)
                } else {
                    _friendsState.value = FriendsState.Error("Failed to fetch chats")
                }
            } catch (e: Exception) {
                _friendsState.value = FriendsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun accessChat(userId: String) {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            try {
                val response = apiService.accessChat(AccessChatRequest(userId))
                if (response.isSuccessful && response.body() != null) {
                    _friendsState.value = FriendsState.ChatAccessed(response.body()!!._id)
                } else {
                    _friendsState.value = FriendsState.Error("Failed to open chat")
                }
            } catch (e: Exception) {
                _friendsState.value = FriendsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _friendsState.value = FriendsState.Idle
    }
}
