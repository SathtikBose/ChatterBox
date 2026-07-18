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
import androidx.compose.material.icons.filled.Close
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
    val isUserOnline by viewModel.isUserOnline.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Could be implemented by saving bitmap to a temp file and passing URI to sendImageMessage
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.sendImageMessage(context, uri)
        }
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
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            // Extract other user name from messages if available
                            val otherUser = messages.firstOrNull { it.sender._id != viewModel.currentUserId }?.sender
                            val titleName = otherUser?.username ?: "Chat"

                            Text(titleName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            if (isTyping) {
                                Text("Typing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            } else if (isUserOnline) {
                                Text("Online", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text("Offline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Green dot indicator for online status
                        if (isUserOnline) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Block User") },
                                onClick = {
                                    showMenu = false
                                    viewModel.blockUser()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (replyingToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ReplyMessagePreview(replyTo = replyingToMessage!!)
                        }
                        IconButton(onClick = { viewModel.setReplyingToMessage(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Photo, contentDescription = "Gallery", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
                    placeholder = { Text("Message...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (chatState == ChatState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    SwipeToReplyMessage(msg, isMine = msg.sender._id == viewModel.currentUserId) {
                        viewModel.setReplyingToMessage(it)
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToReplyMessage(message: MessageDto, isMine: Boolean, onReply: (MessageDto) -> Unit) {
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
                                onReply(message)
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
                SentMessage(message)
            } else {
                ReceivedMessage(message)
            }
        }
    }
}

@Composable
fun SentMessage(message: MessageDto) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                if (message.replyTo != null) {
                    ReplyMessagePreview(replyTo = message.replyTo)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (!message.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Sent image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                if (message.content.isNotBlank()) {
                    Text(message.content, color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ReceivedMessage(message: MessageDto) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                if (message.replyTo != null) {
                    ReplyMessagePreview(replyTo = message.replyTo)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (!message.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Received image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                if (message.content.isNotBlank()) {
                    Text(message.content, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ReplyMessagePreview(replyTo: MessageDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
        Column(modifier = Modifier.padding(8.dp).weight(1f)) {
            Text(replyTo.sender.username ?: "User", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(replyTo.content.ifBlank { "Photo" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
        }
        if (!replyTo.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = replyTo.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

