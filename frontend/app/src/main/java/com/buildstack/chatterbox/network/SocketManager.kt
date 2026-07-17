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

    fun connect(token: String) {
        if (mSocket?.connected() == true) return
        
        try {
            val options = IO.Options().apply {
                auth = mutableMapOf<String, String>("token" to token)
                transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
            }
            // Assuming localhost for backend. Android emulator uses 10.0.2.2 for localhost
            mSocket = IO.socket("http://10.0.2.2:5000", options)
            
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

            mSocket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
        mSocket?.off()
        mSocket = null
        _isConnected.value = false
    }

    fun getSocket(): Socket? = mSocket
}
