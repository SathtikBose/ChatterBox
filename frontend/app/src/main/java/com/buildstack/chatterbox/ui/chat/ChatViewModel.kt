package com.buildstack.chatterbox.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.data.network.MessageDto
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.SendMessageRequest
import com.buildstack.chatterbox.data.network.BlockUserRequest
import com.buildstack.chatterbox.data.network.TokenManager
import com.buildstack.chatterbox.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

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

    private val _replyingToMessage = MutableStateFlow<MessageDto?>(null)
    val replyingToMessage: StateFlow<MessageDto?> = _replyingToMessage.asStateFlow()

    private val _isUserBlocked = MutableStateFlow(false)
    val isUserBlocked: StateFlow<Boolean> = _isUserBlocked.asStateFlow()

    private var currentChatId: String = ""
    
    val currentUserId: String = tokenManager.getUserId() ?: ""
    private var otherUserId: String = ""

    private val _isUserOnline = MutableStateFlow(false)
    val isUserOnline: StateFlow<Boolean> = _isUserOnline.asStateFlow()

    init {
        val token = tokenManager.getToken() ?: ""
        socketManager.connect(token)
        
        viewModelScope.launch {
            socketManager.isConnected.collect { connected ->
                if (connected && currentUserId.isNotEmpty()) {
                    socketManager.setupUser(currentUserId)
                }
            }
        }
        
        viewModelScope.launch {
            socketManager.newMessage.collect { msg ->
                if (msg != null && msg.chatIdString == currentChatId) {
                    val currentList = _messages.value.toMutableList()
                    if (!currentList.any { it._id == msg._id }) {
                        _messages.value = currentList + msg
                    }
                }
            }
        }
        
        viewModelScope.launch {
            socketManager.userOnline.collect { userId ->
                if (userId != null && userId == otherUserId) {
                    _isUserOnline.value = true
                }
            }
        }
        
        viewModelScope.launch {
            socketManager.userOffline.collect { userId ->
                if (userId != null && userId == otherUserId) {
                    _isUserOnline.value = false
                }
            }
        }
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
                    val fetchedMessages = response.body()!!
                    _messages.value = fetchedMessages
                    val otherUser = fetchedMessages.firstOrNull { it.sender._id != currentUserId }?.sender
                    if (otherUser != null) {
                        otherUserId = otherUser._id
                        _isUserOnline.value = otherUser.isOnline == true
                    }
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

    fun setReplyingToMessage(message: MessageDto?) {
        _replyingToMessage.value = message
    }

    fun blockUser() {
        if (otherUserId.isBlank()) return
        viewModelScope.launch {
            _chatState.value = ChatState.Loading
            try {
                val response = apiService.blockUser(BlockUserRequest(otherUserId))
                if (response.isSuccessful) {
                    _isUserBlocked.value = true
                    _chatState.value = ChatState.Idle
                } else {
                    _chatState.value = ChatState.Error("Failed to block user")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendMessage() {
        if (_inputText.value.isBlank() || currentChatId.isBlank()) return
        val text = _inputText.value
        val replyToId = _replyingToMessage.value?._id
        _inputText.value = ""
        _replyingToMessage.value = null
        
        viewModelScope.launch {
            try {
                val response = apiService.sendMessage(SendMessageRequest(currentChatId, text, null, replyToId))
                if (response.isSuccessful && response.body() != null) {
                    val newMessage = response.body()!!
                    _messages.value = _messages.value + newMessage
                    socketManager.emitNewMessage(newMessage)
                } else {
                    _chatState.value = ChatState.Error("Failed to send message")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun sendImageMessage(context: android.content.Context, uri: android.net.Uri) {
        if (currentChatId.isBlank()) return
        
        viewModelScope.launch {
            _chatState.value = ChatState.Loading
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = java.io.File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }
                
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestFile = tempFile.asRequestBody(mediaType)
                val body = okhttp3.MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                
                val uploadResponse = apiService.uploadImage(body)
                if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
                    val imageUrl = uploadResponse.body()!!.imageUrl
                    val replyToId = _replyingToMessage.value?._id
                    _replyingToMessage.value = null
                    
                    val response = apiService.sendMessage(SendMessageRequest(currentChatId, "", imageUrl, replyToId))
                    if (response.isSuccessful && response.body() != null) {
                        val newMessage = response.body()!!
                        _messages.value = _messages.value + newMessage
                        socketManager.emitNewMessage(newMessage)
                        _chatState.value = ChatState.Idle
                    } else {
                        _chatState.value = ChatState.Error("Failed to send image")
                    }
                } else {
                    _chatState.value = ChatState.Error("Failed to upload image")
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
