package com.buildstack.chatterbox.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF35343A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("cool_dev_99", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Typing...", fontSize = 12.sp, color = Color(0xFFBDFF00))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131318),
                    titleContentColor = Color(0xFFE4E1E9),
                    navigationIconContentColor = Color(0xFFE4E1E9),
                    actionIconContentColor = Color(0xFFE4E1E9)
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131318))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Camera */ }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color(0xFFC2CAAD))
                }
                IconButton(onClick = { /* Gallery */ }) {
                    Icon(Icons.Default.Photo, contentDescription = "Gallery", tint = Color(0xFFC2CAAD))
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Message...", color = Color(0xFF8C9479)) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFBDFF00),
                        unfocusedBorderColor = Color(0xFF434933),
                        focusedContainerColor = Color(0xFF1B1B20),
                        unfocusedContainerColor = Color(0xFF1B1B20),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                IconButton(
                    onClick = { /* Send */ },
                    modifier = Modifier.background(Color(0xFFBDFF00), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF131318)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            // Dummy messages
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SentMessage("I'm doing well, working on the app! 😎") }
            item { ReceivedMessage("Hey! How are you?") }
        }
    }
}

@Composable
fun SentMessage(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .background(Color(0xFFBDFF00), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(text, color = Color.Black, fontSize = 15.sp)
        }
    }
}

@Composable
fun ReceivedMessage(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1B1B20), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(text, color = Color.White, fontSize = 15.sp)
        }
    }
}
