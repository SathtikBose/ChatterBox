package com.buildstack.chatterbox

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.buildstack.chatterbox.ui.login.LoginScreen
import com.buildstack.chatterbox.ui.login.RegisterScreen
import com.buildstack.chatterbox.ui.login.ForgotPasswordScreen
import com.buildstack.chatterbox.ui.friends.FriendsScreen
import com.buildstack.chatterbox.ui.chat.ChatScreen
import com.buildstack.chatterbox.ui.profile.ProfileScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(
            onLoginSuccess = { backStack.add(Friends) },
            onNavigateToRegister = { backStack.add(Register) },
            onNavigateToForgotPassword = { backStack.add(ForgotPassword) }
          )
        }
        entry<Register> {
          RegisterScreen(
            onRegisterSuccess = { backStack.add(Friends) },
            onNavigateToLogin = { backStack.removeLastOrNull() }
          )
        }
        entry<ForgotPassword> {
          ForgotPasswordScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onSendOtp = { email -> /* TODO: Navigate to OTP verification or show toast */ }
          )
        }
        entry<Friends> {
          FriendsScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToChat = { friendId -> backStack.add(Chat(friendId)) },
            onNavigateToProfile = { backStack.add(Profile) }
          )
        }
        entry<Chat> { navKey ->
          ChatScreen(
            friendId = navKey.friendId,
            onNavigateBack = { backStack.removeLastOrNull() }
          )
        }
        entry<Profile> {
          ProfileScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onLogout = {
                // Clear backstack and go to login
                while(backStack.size > 1) { backStack.removeLastOrNull() }
                if(backStack.size == 1 && backStack.first() != Login) {
                    backStack.removeLastOrNull()
                    backStack.add(Login)
                }
            }
          )
        }
      },
  )
}
