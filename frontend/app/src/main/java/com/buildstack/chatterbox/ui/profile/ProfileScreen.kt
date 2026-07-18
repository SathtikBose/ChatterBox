package com.buildstack.chatterbox.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    
    val currentUsername by viewModel.username.collectAsState()
    var username by remember(currentUsername) { mutableStateOf(currentUsername) }

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                Toast.makeText(context, (profileState as ProfileState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProfileState.Error -> {
                Toast.makeText(context, (profileState as ProfileState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131318),
                    titleContentColor = Color(0xFFE4E1E9),
                    navigationIconContentColor = Color(0xFFE4E1E9)
                )
            )
        },
        containerColor = Color(0xFF131318)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF35343A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFFBDFF00)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBDFF00),
                    unfocusedBorderColor = Color(0xFF434933),
                    focusedLabelColor = Color(0xFFBDFF00),
                    unfocusedLabelColor = Color(0xFF8C9479),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    viewModel.updateProfile(username, "")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBDFF00),
                    contentColor = Color.Black
                ),
                enabled = profileState != ProfileState.Loading
            ) {
                if (profileState == ProfileState.Loading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF93000A),
                    contentColor = Color.White
                )
            ) {
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
