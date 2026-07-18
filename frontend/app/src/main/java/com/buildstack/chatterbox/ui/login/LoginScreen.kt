package com.buildstack.chatterbox.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onLoginSuccess()
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
        verticalArrangement = Arrangement.Center
    ) {
        Text("Chatter Box", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Connect with your vibe.", color = Color(0xFFBDFF00))
        Spacer(modifier = Modifier.height(48.dp))
        
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
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(0xFF8C9479)) },
            visualTransformation = PasswordVisualTransformation(),
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
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Forgot Password?", color = Color(0xFFC2CAAD))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.login(email, password)
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
                Text("Login", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register", color = Color(0xFFBDFF00))
        }
    }
}
