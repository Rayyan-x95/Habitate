package com.ninety5.habitate.ui.screens.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.ninety5.habitate.data.local.relation.StoryWithUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val STORY_DURATION_MS = 5000L // 5 seconds per story

@Composable
fun StoryViewerScreen(
    userId: String,
    onClose: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val stories by viewModel.activeStories.collectAsState()
    val userStories = stories.filter { it.story.userId == userId }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Mark story as seen when viewed
    LaunchedEffect(currentIndex, userStories) {
        if (userStories.isNotEmpty() && currentIndex < userStories.size) {
            viewModel.markAsSeen(userStories[currentIndex].story.id)
        }
    }

    // Auto-progress timer - only key on currentIndex and userStories, handle pause internally
    LaunchedEffect(currentIndex, userStories) {
        if (userStories.isEmpty()) return@LaunchedEffect
        
        progress.snapTo(0f)
        
        // Use snapshotFlow to reactively observe isPaused changes
        snapshotFlow { isPaused to progress.value }
            .collectLatest { (paused, currentProgress) ->
                if (!paused && currentProgress < 1f) {
                    val remaining = 1f - currentProgress
                    val remainingDuration = (remaining * STORY_DURATION_MS).toLong()
                    
                    try {
                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = remainingDuration.toInt(),
                                easing = LinearEasing
                            )
                        )
                        
                        // Move to next story when progress completes
                        if (progress.value >= 1f) {
                            if (currentIndex < userStories.size - 1) {
                                currentIndex++
                            } else {
                                onClose()
                            }
                        }
                    } catch (e: Exception) {
                        // Animation was cancelled (pause or navigation)
                    }
                }
            }
    }

    // Pause timer when screen is not visible
    DisposableEffect(Unit) {
        onDispose { isPaused = true }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width
                        if (offset.x < width / 3) {
                            // Tap left - previous story
                            if (currentIndex > 0) {
                                currentIndex--
                                scope.launch { progress.snapTo(0f) }
                            }
                        } else if (offset.x > width * 2 / 3) {
                            // Tap right - next story
                            if (currentIndex < userStories.size - 1) {
                                currentIndex++
                                scope.launch { progress.snapTo(0f) }
                            } else {
                                onClose()
                            }
                        }
                    },
                    onLongPress = { isPaused = true },
                    onPress = {
                        awaitRelease()
                        isPaused = false
                    }
                )
            }
    ) {
        if (userStories.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No stories available",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stories expire after 24 hours",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            val currentStory = userStories.getOrNull(currentIndex)
            
            if (currentStory != null) {
                // Story content - Image
                StoryContent(
                    storyWithUser = currentStory,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Top gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                // Progress indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    userStories.forEachIndexed { index, _ ->
                        val segmentProgress = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> progress.value
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
                
                // User info header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    AsyncImage(
                        model = currentStory.user?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = currentStory.user?.displayName ?: "User",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTimeAgo(currentStory.story.createdAt),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Pause/Play indicator
                    if (isPaused) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Paused",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Caption (if present)
                currentStory.story.caption?.let { caption ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .padding(16.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = caption,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StoryContent(
    storyWithUser: StoryWithUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaUrl = storyWithUser.story.mediaUrl
    
    // Determine if media is video by file extension, handling URLs with query strings
    val isVideo = remember(mediaUrl) {
        val pathSegment = android.net.Uri.parse(mediaUrl).lastPathSegment ?: ""
        pathSegment.endsWith(".mp4", ignoreCase = true) ||
        pathSegment.endsWith(".webm", ignoreCase = true) ||
        pathSegment.endsWith(".mov", ignoreCase = true) ||
        pathSegment.endsWith(".avi", ignoreCase = true)
    }
    
    if (isVideo) {
        // For video, show a placeholder with video icon
        // Full video player implementation would require ExoPlayer
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(16.dp)
                )
                Text(
                    text = "Video stories coming soon",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        // Image story
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(mediaUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Story image",
            modifier = modifier,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load image",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        )
    }
}

private fun formatTimeAgo(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestampMillis
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestampMillis))
    }
}
