package com.buildstack.chatterbox.ui.chat

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.buildstack.chatterbox.data.network.MessageDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friendId: String, // This is actually the chatId now
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Handle bitmap
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Handle uri
    }

    LaunchedEffect(friendId) {
        viewModel.initializeChat(friendId)
    }

    LaunchedEffect(chatState) {
        if (chatState is ChatState.Error) {
            Toast.makeText(context, (chatState as ChatState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

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
                            // Extract other user name from messages if available
                            val otherUser = messages.firstOrNull { it.sender._id != viewModel.currentUserId }?.sender
                            val titleName = otherUser?.username ?: "Chat"

                            Text(titleName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (isTyping) {
                                Text("Typing...", fontSize = 12.sp, color = Color(0xFFBDFF00))
                            } else if (otherUser?.isOnline == true) {
                                Text("Online", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                Text("Offline", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Green dot indicator for online status
                        val isOnline = messages.firstOrNull { it.sender._id != viewModel.currentUserId }?.sender?.isOnline == true
                        if (isOnline) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFBDFF00)))
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
                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color(0xFFC2CAAD))
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Photo, contentDescription = "Gallery", tint = Color(0xFFC2CAAD))
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
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
                    onClick = { viewModel.sendMessage() },
                    modifier = Modifier.background(Color(0xFFBDFF00), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF131318)
    ) { paddingValues ->
        if (chatState == ChatState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFBDFF00))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                // Reverse messages because LazyColumn is reversed
                items(messages.reversed()) { msg ->
                    SwipeToReplyMessage(msg, isMine = msg.sender._id == viewModel.currentUserId)
                }
            }
        }
    }
}

@Composable
fun SwipeToReplyMessage(message: MessageDto, isMine: Boolean) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value < -100f) {
                                // Trigger reply action here
                            }
                            offsetX.animateTo(0f) // Micro-interaction snap back
                        }
                    }
                ) { change, dragAmount ->
                    if (dragAmount < 0 || offsetX.value < 0) { // only swipe left
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            if (isMine) {
                SentMessage(message.content)
            } else {
                ReceivedMessage(message.content)
            }
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
