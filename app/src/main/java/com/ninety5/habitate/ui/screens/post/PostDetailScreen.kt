package com.ninety5.habitate.ui.screens.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ninety5.habitate.ui.components.designsystem.HabitateErrorState
import com.ninety5.habitate.ui.components.UserAvatar
import com.ninety5.habitate.ui.components.PostItem
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.ui.theme.HabitateDarkGreenStart
import com.ninety5.habitate.ui.theme.HabitateOffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = HabitateDarkGreenStart,
        topBar = {
            TopAppBar(
                title = { Text("Post", color = HabitateOffWhite) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = HabitateOffWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateDarkGreenStart,
                    titleContentColor = HabitateOffWhite,
                    navigationIconContentColor = HabitateOffWhite
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(commentText)
                            commentText = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                HabitateErrorState(
                    title = "Something went wrong",
                    description = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.clearError() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.post != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Post content
                    item {
                        val postUiModel = uiState.post?.let { post ->
                            PostUiModel(
                                id = post.id,
                                authorId = uiState.author?.id ?: "",
                                authorName = uiState.author?.displayName ?: "Unknown",
                                authorAvatarUrl = uiState.author?.avatarUrl,
                                contentText = post.contentText ?: "",
                                mediaUrls = post.mediaUrls,
                                likes = uiState.likeCount,
                                comments = uiState.commentCount,
                                shares = 0,
                                isLiked = uiState.isLiked,
                                reactionType = null,
                                visibility = post.visibility,
                                createdAt = post.createdAt.toString(),
                                workoutSummary = null
                            )
                        }

                        if (postUiModel != null) {
                            PostItem(
                                post = postUiModel,
                                onLikeClick = { viewModel.toggleLike() },
                                onReactionClick = { reaction -> viewModel.toggleLike(reaction) },
                                onCommentClick = { /* Already on detail screen */ },
                                onShareClick = { /* Share */ },
                                onUserClick = { onUserClick(postUiModel.authorId) }
                            )
                        }

                        HorizontalDivider(color = HabitateOffWhite.copy(alpha = 0.1f))

                        Text(
                            text = "Comments",
                            style = MaterialTheme.typography.titleMedium,
                            color = HabitateOffWhite,
                            modifier = Modifier.padding(16.dp)

                        )
                    }

                    // Comments list
                    if (uiState.comments.isEmpty()) {
                        item {
                            Text(
                                text = "No comments yet. Be the first!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(uiState.comments, key = { it.comment.id }) { commentWithUser ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                UserAvatar(avatarUrl = commentWithUser.user.avatarUrl, size = 32.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = commentWithUser.user.displayName,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = commentWithUser.comment.text,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
