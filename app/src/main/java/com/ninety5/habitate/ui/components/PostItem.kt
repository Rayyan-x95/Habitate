package com.ninety5.habitate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.ui.theme.HabitateDarkGreenStart
import com.ninety5.habitate.ui.theme.HabitateDarkGreenEnd
import com.ninety5.habitate.ui.theme.HabitateOffWhite
import com.ninety5.habitate.ui.theme.SageGreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: PostUiModel,
    onLikeClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onUserClick)
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = post.createdAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        Icons.Rounded.MoreHoriz,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Text
            if (post.contentText.isNotEmpty()) {
                Text(
                    text = post.contentText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
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
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                    targetValue = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "Like Color"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    var showReactions by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 44.dp, minHeight = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isLiked) "Unlike" else "Like",
                            tint = tint,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(if (isLiked) scale else 1f)
                                .combinedClickable(
                                    onClick = onLikeClick,
                                    onLongClick = { showReactions = true }
                                )
                        )
                    }

                    DropdownMenu(
                        expanded = showReactions,
                        onDismissRequest = { showReactions = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
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
                    
                    if (post.likes > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${post.likes}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
                        .clickable(onClick = onCommentClick)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    if (post.comments > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${post.comments}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Share Button
                IconButton(onClick = onShareClick) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
