package com.buildstack.chatterbox.ui.friends

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
fun FriendsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val friendsState by viewModel.friendsState.collectAsState()

    // Fetch initial chats
    LaunchedEffect(Unit) {
        viewModel.fetchChats()
    }

    LaunchedEffect(searchQuery) {
        viewModel.searchUsers(searchQuery)
    }

    LaunchedEffect(friendsState) {
        when (friendsState) {
            is FriendsState.Error -> {
                Toast.makeText(context, (friendsState as FriendsState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is FriendsState.ChatAccessed -> {
                val chatId = (friendsState as FriendsState.ChatAccessed).chatId
                viewModel.resetState()
                onNavigateToChat(chatId)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131318),
                    titleContentColor = Color(0xFFE4E1E9),
                    navigationIconContentColor = Color(0xFFE4E1E9)
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFFBDFF00))
                    }
                }
            )
        },
        containerColor = Color(0xFF131318)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBDFF00),
                    unfocusedBorderColor = Color(0xFF434933),
                    focusedContainerColor = Color(0xFF1B1B20),
                    unfocusedContainerColor = Color(0xFF1B1B20),
                    focusedPlaceholderColor = Color(0xFF8C9479),
                    unfocusedPlaceholderColor = Color(0xFF8C9479),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            if (friendsState == FriendsState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFBDFF00))
                }
            } else if (searchQuery.isNotBlank() && friendsState is FriendsState.UsersLoaded) {
                val loadedState = friendsState as FriendsState.UsersLoaded
                Text("Search Results", color = Color(0xFFC2CAAD), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(loadedState.users) { user ->
                        SearchResultItem(
                            username = user.username,
                            onChatClick = { viewModel.accessChat(user._id) }
                        )
                    }
                }
            } else if (searchQuery.isBlank() && friendsState is FriendsState.ChatsLoaded) {
                val loadedState = friendsState as FriendsState.ChatsLoaded
                Text("Recent Chats", color = Color(0xFFC2CAAD), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val chats = loadedState.chats
                    if (chats.isEmpty()) {
                        item {
                            Text("No recent chats. Search for friends to start chatting!", color = Color.Gray)
                        }
                    }
                    items(chats) { chat ->
                        val otherUser = chat.participants.firstOrNull() // Simplify for now
                        val chatName = if (chat.isGroupChat) chat.chatName else otherUser?.username ?: "Unknown"
                        
                        ChatResultItem(
                            chatName = chatName,
                            lastMessage = chat.latestMessage?.content ?: "Start chatting!",
                            onClick = { onNavigateToChat(chat._id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    username: String,
    onChatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1B20), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF35343A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(username, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = onChatClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35343A), contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Chat")
        }
    }
}

@Composable
fun ChatResultItem(
    chatName: String,
    lastMessage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1B20), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF35343A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(chatName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
    }
}
