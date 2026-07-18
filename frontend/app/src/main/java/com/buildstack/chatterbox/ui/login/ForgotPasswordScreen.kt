package com.buildstack.chatterbox.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onSendOtp: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    
    var email by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.ForgotPasswordSuccess -> {
                Toast.makeText(context, "Password reset OTP sent to email!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSendOtp(email)
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131318))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Reset Password", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter your email address and we'll send you an OTP to reset your password.",
            color = Color(0xFF8C9479),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color(0xFF8C9479)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBDFF00),
                unfocusedBorderColor = Color(0xFF434933),
                focusedContainerColor = Color(0xFF1B1B20),
                unfocusedContainerColor = Color(0xFF1B1B20),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                if (email.isBlank()) {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.forgotPassword(email)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDFF00)),
            enabled = authState != AuthState.Loading
        ) {
            if (authState == AuthState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Send OTP", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
