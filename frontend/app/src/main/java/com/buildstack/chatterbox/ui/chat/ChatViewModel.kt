package com.buildstack.chatterbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildstack.chatterbox.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(
    val id: String,
    val senderId: String,
    val text: String,
    val isMine: Boolean,
    val imageUrl: String? = null
)

class ChatViewModel(
    private val socketManager: SocketManager = SocketManager()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        // connect with dummy token for now
        socketManager.connect("dummy_token")
        setupSocketListeners()
    }

    private fun setupSocketListeners() {
        val socket = socketManager.getSocket()
        socket?.on("receiveMessage") { args ->
            val data = args[0] as? org.json.JSONObject
            data?.let {
                val newMsg = Message(
                    id = it.optString("id", System.currentTimeMillis().toString()),
                    senderId = it.optString("senderId", "other"),
                    text = it.optString("text", ""),
                    isMine = false
                )
                _messages.value = listOf(newMsg) + _messages.value
            }
        }
        
        socket?.on("typing") {
            _isTyping.value = true
        }

        socket?.on("stopTyping") {
            _isTyping.value = false
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        socketManager.getSocket()?.emit("typing")
        // normally you'd debounce a stopTyping event
    }

    fun sendMessage() {
        if (_inputText.value.isBlank()) return
        val text = _inputText.value
        val msg = Message(
            id = System.currentTimeMillis().toString(),
            senderId = "me",
            text = text,
            isMine = true
        )
        _messages.value = listOf(msg) + _messages.value
        _inputText.value = ""
        
        val json = org.json.JSONObject().apply {
            put("text", text)
        }
        socketManager.getSocket()?.emit("sendMessage", json)
        socketManager.getSocket()?.emit("stopTyping")
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
