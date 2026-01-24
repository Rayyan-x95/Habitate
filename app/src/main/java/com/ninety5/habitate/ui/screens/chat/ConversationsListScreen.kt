package com.ninety5.habitate.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninety5.habitate.ui.theme.*
import com.ninety5.habitate.ui.theme.LocalHabitateColors

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                  HABITATE CONVERSATIONS LIST SCREEN (PIXEL-PERFECT)       ║
 * ║                                                                          ║
 * ║  Replicates the chat/messages screen from reference images:               ║
 * ║  • Top bar with "Messages" title and search icon                         ║
 * ║  • List of conversations with user avatars                               ║
 * ║  • User names, last messages, and timestamps                            ║
 * ║  • Online status indicators                                             ║
 * ║  • Clean, minimal design matching reference                              ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

@Composable
fun ConversationsListScreen(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReferenceColors.backgroundLight)
    ) {
        // Top Bar
        ConversationsTopBar(
            onNavigateToSearch = onNavigateToSearch,
            onNavigateBack = onNavigateBack
        )
        
        // Conversations List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = RefSpacingSM.dp),
            verticalArrangement = Arrangement.spacedBy(RefSpacingXS.dp)
        ) {
            items(getSampleConversations()) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    onClick = { onNavigateToConversation(conversation.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationsTopBar(
    onNavigateToSearch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReferenceColors.surface)
            .padding(RefSpacingMD.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(RefIconSizeXL.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ReferenceColors.textPrimary,
                modifier = Modifier.size(RefIconSizeMD.dp)
            )
        }
        
        // Title
        Text(
            text = "Messages",
            fontSize = RefTextSizeLG.sp,
            fontWeight = FontWeight.SemiBold,
            color = ReferenceColors.textPrimary
        )
        
        // Search button
        IconButton(
            onClick = onNavigateToSearch,
            modifier = Modifier.size(RefIconSizeXL.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ReferenceColors.textPrimary,
                modifier = Modifier.size(RefIconSizeMD.dp)
            )
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(RefRadiusLG.dp),
        colors = CardDefaults.cardColors(
            containerColor = ReferenceColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = RefCardElevation.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RefSpacingMD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status
            Box(
                modifier = Modifier.size(RefAvatarSizeLG.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(RefAvatarSizeLG.dp)
                        .clip(CircleShape)
                        .background(ReferenceColors.border),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.userName.first().toString(),
                        fontSize = RefTextSizeXL.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReferenceColors.textSecondary
                    )
                }
                
                // Online status indicator
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(LocalHabitateColors.current.success)
                            .align(Alignment.BottomEnd)
                            .border(
                                width = 2.dp,
                                color = ReferenceColors.surface,
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(RefSpacingMD.dp))
            
            // Conversation details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = conversation.userName,
                        fontSize = RefTextSizeMD.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ReferenceColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = conversation.timestamp,
                        fontSize = RefTextSizeXS.sp,
                        color = ReferenceColors.textSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(RefSpacingXS.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = RefTextSizeSM.sp,
                        color = if (conversation.isUnread) ReferenceColors.textPrimary else ReferenceColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (conversation.isUnread) FontWeight.SemiBold else FontWeight.Normal
                    )
                    
                    // Unread indicator
                    if (conversation.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ReferenceColors.accent)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sample conversation data
 */
data class Conversation(
    val id: String,
    val userName: String,
    val lastMessage: String,
    val timestamp: String,
    val isOnline: Boolean,
    val isUnread: Boolean
)

private fun getSampleConversations(): List<Conversation> {
    return listOf(
        Conversation(
            id = "1",
            userName = "Sarah Johnson",
            lastMessage = "Hey! Are you free for a workout session tomorrow?",
            timestamp = "2m ago",
            isOnline = true,
            isUnread = true
        ),
        Conversation(
            id = "2",
            userName = "Mike Chen",
            lastMessage = "Great run today! Thanks for the motivation 💪",
            timestamp = "15m ago",
            isOnline = true,
            isUnread = true
        ),
        Conversation(
            id = "3",
            userName = "Emma Davis",
            lastMessage = "The meditation session was so helpful",
            timestamp = "1h ago",
            isOnline = false,
            isUnread = false
        ),
        Conversation(
            id = "4",
            userName = "Alex Thompson",
            lastMessage = "See you at the community event!",
            timestamp = "2h ago",
            isOnline = false,
            isUnread = false
        ),
        Conversation(
            id = "5",
            userName = "Lisa Martinez",
            lastMessage = "Thanks for the healthy recipe recommendations",
            timestamp = "3h ago",
            isOnline = true,
            isUnread = false
        ),
        Conversation(
            id = "6",
            userName = "James Wilson",
            lastMessage = "Looking forward to our hiking trip this weekend",
            timestamp = "5h ago",
            isOnline = false,
            isUnread = false
        ),
        Conversation(
            id = "7",
            userName = "Nutrition Group",
            lastMessage = "New meal plan shared in the group",
            timestamp = "1d ago",
            isOnline = false,
            isUnread = false
        )
    )
}
