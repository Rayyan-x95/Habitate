package com.ninety5.habitate.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.ui.components.ExperimentalFeatureBanner
import com.ninety5.habitate.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    roomId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = viewModel.currentUserId
    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) } // Should come from VM

    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    LaunchedEffect(inputText) {
        viewModel.sendTyping(inputText.isNotEmpty())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Chat")
                        if (typingUsers.isNotEmpty()) {
                            Text(
                                "Typing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (isMuted) "Unmute" else "Mute") },
                            onClick = {
                                isMuted = !isMuted
                                viewModel.toggleMute(roomId, isMuted)
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") }
                )
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ExperimentalFeatureBanner(
                featureName = "Real-time Chat",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (isLoading && messages.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageItem(
                            message = message,
                            currentUserId = currentUserId,
                            onReact = { emoji -> viewModel.addReaction(message.message.id, emoji) },
                            onDelete = { viewModel.deleteMessage(message.message.id) },
                            onSeen = { viewModel.markAsRead(message.message.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: MessageWithReactions,
    currentUserId: String?,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onSeen: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isMe = message.message.senderId == currentUserId

    LaunchedEffect(Unit) {
        if (!isMe && message.message.status != com.ninety5.habitate.data.local.entity.MessageStatus.READ) {
            onSeen()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = message.message.content ?: "")
                if (message.reactions.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        message.reactions.forEach { reaction ->
                            Text(
                                text = reaction.emoji,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        if (isMe) {
            Text(
                text = if (message.message.status == com.ninety5.habitate.data.local.entity.MessageStatus.READ) "Read" else "Sent",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("React ❤️") },
                onClick = { onReact("❤️"); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("React 👍") },
                onClick = { onReact("👍"); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { onDelete(); showMenu = false }
            )
        }
    }
}
