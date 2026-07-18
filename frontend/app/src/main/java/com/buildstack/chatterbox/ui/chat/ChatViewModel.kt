package com.buildstack.chatterbox.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.MessageDto
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.SendMessageRequest
import com.buildstack.chatterbox.data.network.TokenManager
import com.buildstack.chatterbox.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.apiService
    private val socketManager = SocketManager()
    private val tokenManager = TokenManager(application)
    
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Idle)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var currentChatId: String = ""
    
    val currentUserId: String = tokenManager.getUserId() ?: ""

    init {
        val token = tokenManager.getToken() ?: ""
        socketManager.connect(token)
    }

    fun initializeChat(chatId: String) {
        currentChatId = chatId
        socketManager.joinChat(chatId)
        fetchMessages(chatId)
    }

    private fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            _chatState.value = ChatState.Loading
            try {
                val response = apiService.allMessages(chatId)
                if (response.isSuccessful && response.body() != null) {
                    _messages.value = response.body()!!
                    _chatState.value = ChatState.Idle
                } else {
                    _chatState.value = ChatState.Error("Failed to fetch messages")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        if (_inputText.value.isBlank() || currentChatId.isBlank()) return
        val text = _inputText.value
        _inputText.value = ""
        
        viewModelScope.launch {
            try {
                val response = apiService.sendMessage(SendMessageRequest(text, currentChatId))
                if (response.isSuccessful && response.body() != null) {
                    val newMessage = response.body()!!
                    _messages.value = _messages.value + newMessage
                } else {
                    _chatState.value = ChatState.Error("Failed to send message")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _chatState.value = ChatState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
