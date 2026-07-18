package com.buildstack.chatterbox.data.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "chatter_box_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String, userId: String, username: String, email: String, profilePic: String) {
        sharedPreferences.edit()
            .putString("jwt_token", token)
            .putString("user_id", userId)
            .putString("username", username)
            .putString("email", email)
            .putString("profile_pic", profilePic)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    fun getUserId(): String? = sharedPreferences.getString("user_id", null)
    fun getUsername(): String? = sharedPreferences.getString("username", null)
    fun getEmail(): String? = sharedPreferences.getString("email", null)
    fun getProfilePic(): String? = sharedPreferences.getString("profile_pic", null)

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }
}
