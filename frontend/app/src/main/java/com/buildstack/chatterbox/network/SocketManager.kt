package com.buildstack.chatterbox.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {
    private var mSocket: Socket? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // Expose flows for events
    private val _userOnline = MutableStateFlow<String?>(null)
    val userOnline: StateFlow<String?> = _userOnline

    private val _userOffline = MutableStateFlow<String?>(null)
    val userOffline: StateFlow<String?> = _userOffline

    fun connect(token: String) {
        if (mSocket?.connected() == true) return
        
        try {
            val options = IO.Options().apply {
                auth = mutableMapOf<String, String>("token" to token)
                transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
            }
            // Remote backend
            mSocket = IO.socket("https://chatterbox-6w3a.onrender.com/", options)
            
            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected to Socket.IO server")
                _isConnected.value = true
            }

            mSocket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketManager", "Disconnected from Socket.IO server")
                _isConnected.value = false
            }

            mSocket?.on("connect_error") { args ->
                Log.e("SocketManager", "Connection Error: ${args.contentToString()}")
            }

            // Presence Listeners
            mSocket?.on("user online") { args ->
                if (args.isNotEmpty()) {
                    _userOnline.value = args[0] as? String
                }
            }

            mSocket?.on("user offline") { args ->
                if (args.isNotEmpty()) {
                    _userOffline.value = args[0] as? String
                }
            }

            mSocket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun setupUser(userId: String) {
        val json = JSONObject().apply {
            put("_id", userId)
        }
        mSocket?.emit("setup", json)
    }

    fun joinChat(chatId: String) {
        mSocket?.emit("join chat", chatId)
    }

    fun disconnect() {
        mSocket?.disconnect()
        mSocket?.off()
        mSocket = null
        _isConnected.value = false
    }

    fun getSocket(): Socket? = mSocket
}
