package com.ninety5.habitate.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@Composable
fun ConversationsListScreen(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val colors = HabitateTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        ConversationsTopBar(
            onNavigateToSearch = onNavigateToSearch,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
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
    val colors = HabitateTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(Size.iconXl)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.onBackground,
                modifier = Modifier.size(Size.iconMd)
            )
        }

        Text(
            text = "Messages",
            style = HabitateTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground
        )

        IconButton(
            onClick = onNavigateToSearch,
            modifier = Modifier.size(Size.iconXl)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.onBackground,
                modifier = Modifier.size(Size.iconMd)
            )
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val colors = HabitateTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Conversation: ${conversation.userName}" }
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status
            Box(modifier = Modifier.size(Size.avatarMd)) {
                Box(
                    modifier = Modifier
                        .size(Size.avatarMd)
                        .clip(CircleShape)
                        .background(colors.border.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.userName.first().toString(),
                        style = HabitateTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground.copy(alpha = 0.6f)
                    )
                }

                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors.success)
                            .align(Alignment.BottomEnd)
                            .border(
                                width = 2.dp,
                                color = colors.surface,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = conversation.userName,
                        style = HabitateTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = conversation.timestamp,
                        style = HabitateTheme.typography.labelSmall,
                        color = colors.onBackground.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = HabitateTheme.typography.bodyMedium,
                        color = if (conversation.isUnread) colors.onBackground else colors.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (conversation.isUnread) FontWeight.SemiBold else FontWeight.Normal
                    )

                    if (conversation.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                    }
                }
            }
        }
    }
}

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
