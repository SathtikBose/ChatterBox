package com.buildstack.chatterbox

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Friends : NavKey
@Serializable data class Chat(val friendId: String) : NavKey
@Serializable data object Profile : NavKey
