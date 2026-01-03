package com.ninety5.habitate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ninety5.habitate.ui.screens.feed.PostUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: PostUiModel,
    currentUserId: String? = null,
    onLikeClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCommentClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = post.authorAvatarUrl,
                contentDescription = "Avatar of ${post.authorName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onUserClick
                    ),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = post.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { /* Menu */ }) {
                Icon(
                    Icons.Rounded.MoreVert, 
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (post.contentText.isNotBlank()) {
            Text(
                text = post.contentText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Media
        if (post.mediaUrls.isNotEmpty()) {
            AsyncImage(
                model = post.mediaUrls.first(),
                contentDescription = "Post attachment",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Button with Animation
            val isLiked = post.isLiked
            val scale by animateFloatAsState(
                targetValue = if (isLiked) 1.2f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f),
                label = "Like Scale"
            )
            val tint by animateColorAsState(
                targetValue = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "Like Color"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                var showReactions by remember { mutableStateOf(false) }
                
                Box {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = onLikeClick,
                                onLongClick = { showReactions = true }
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isLiked) "Unlike" else "Like",
                            tint = tint,
                            modifier = Modifier.scale(if (isLiked) scale else 1f)
                        )
                    }

                    DropdownMenu(
                        expanded = showReactions,
                        onDismissRequest = { showReactions = false }
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            listOf("❤️", "🔥", "👏", "😢", "😂").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    modifier = Modifier
                                        .clickable { 
                                            onReactionClick(emoji)
                                            showReactions = false
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
                
                // Show reaction if liked (Private Reactions UI)
                if (isLiked) {
                    Text(
                        text = if (post.reactionType != null && post.reactionType != "HEART") post.reactionType else "You liked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Comment Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCommentClick) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (post.comments > 0) {
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Share Button
            IconButton(onClick = onShareClick) {
                Icon(
                    Icons.Rounded.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    contentDescription: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        if (text.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}
