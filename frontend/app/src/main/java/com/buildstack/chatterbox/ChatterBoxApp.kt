package com.buildstack.chatterbox

import android.app.Application
import com.buildstack.chatterbox.data.network.RetrofitClient
import com.buildstack.chatterbox.data.network.TokenManager

class ChatterBoxApp : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
    }

    companion object {
        lateinit var instance: ChatterBoxApp
            private set
    }
}
