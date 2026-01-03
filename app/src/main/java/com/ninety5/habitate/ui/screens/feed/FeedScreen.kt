package com.ninety5.habitate.ui.screens.feed

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import com.ninety5.habitate.data.local.relation.StoryWithUser
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PostAdd
import com.ninety5.habitate.core.utils.DebugLogger
import com.ninety5.habitate.ui.components.HabitateLogo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ninety5.habitate.ui.components.PostItem
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateSkeletonPost
import com.ninety5.habitate.ui.theme.*

import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.IconButton
import com.ninety5.habitate.ui.viewmodel.StoryViewModel
import com.ninety5.habitate.ui.viewmodel.FeatureFlagsViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onChatClick: () -> Unit,
    onStoryClick: (String) -> Unit,
    onAddStoryClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel(),
    featureFlagsViewModel: FeatureFlagsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val storyUiState by storyViewModel.uiState.collectAsState()
    val groupedStories = storyUiState.groupedStories
    val posts = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val featureFlags = featureFlagsViewModel.featureFlags
    // #region agent log
    LaunchedEffect(Unit) {
        DebugLogger.log(
            "FeedScreen.kt:88",
            "Feature flags accessed",
            mapOf("isChatEnabled" to featureFlags.isChatEnabled, "isStoriesEnabled" to featureFlags.isStoriesEnabled, "storiesCount" to groupedStories.size),
            "B"
        )
    }
    // #endregion
    val showStories = featureFlags.isStoriesEnabled && groupedStories.isNotEmpty()
    val colors = HabitateTheme.colors

    Scaffold(
        containerColor = colors.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Glass Header with brand styling
            GlassNavBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HabitateLogo(
                            size = 32.dp,
                            tint = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Text(
                            text = "Habitate",
                            style = SectionTitle,
                            color = colors.textPrimary
                        )
                    }
                    
                    Row {
                        IconButton(onClick = onCheckInClick) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Daily Check-in",
                                tint = colors.textPrimary
                            )
                        }
                        if (featureFlags.isChatEnabled) {
                            IconButton(onClick = onChatClick) {
                                Icon(
                                    imageVector = Icons.Rounded.Chat,
                                    contentDescription = "Chat",
                                    tint = colors.textPrimary
                                )
                            }
                        }
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(Size.fabMedium)
                    .clip(CircleShape)
                    .background(GradientBrand)
                    .clickable(onClick = onCreatePostClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add, 
                    contentDescription = "Create Post",
                    tint = colors.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(paddingValues)
        ) {
            
            if (posts.loadState.refresh is LoadState.Loading) {
                FeedSkeletonLoader()
            } else if (posts.itemCount == 0) {
                EmptyFeedState(onCreatePostClick = onCreatePostClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    if (showStories) {
                        item {
                            StoriesBar(
                                groupedStories = groupedStories,
                                onStoryClick = onStoryClick,
                                onAddStoryClick = onAddStoryClick
                            )
                        }
                    }
                    
                    items(
                        count = posts.itemCount,
                        key = posts.itemKey { it.id },
                        contentType = posts.itemContentType { "post" }
                    ) { index ->
                        val post = posts[index]
                        if (post != null) {
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onReactionClick = { reaction -> viewModel.toggleLike(post.id, reaction) },
                                onCommentClick = { onPostClick(post.id) },
                                onShareClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Check out this post by ${post.authorName}: ${post.contentText}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                onUserClick = { onUserClick(post.authorId) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp, 
                                color = colors.divider
                            )
                        }
                    }
                }
            }
            
            if (uiState.error != null) {
                // Show error snackbar or dialog instead of replacing content
            }
        }
    }
}

@Composable
fun EmptyFeedState(onCreatePostClick: () -> Unit) {
    HabitateEmptyState(
        icon = Icons.Rounded.PostAdd,
        title = "Your Feed is Empty",
        description = "Follow people or join habitats to see posts here. Or be the first to share something!",
        actionText = "Create First Post",
        onAction = onCreatePostClick
    )
}

@Composable
fun FeedSkeletonLoader() {
    Column(Modifier.padding(Spacing.lg)) {
        repeat(4) {
            HabitateSkeletonPost(
                modifier = Modifier.padding(bottom = Spacing.lg)
            )
        }
    }
}

@Composable
fun StoriesBar(
    groupedStories: Map<String, List<StoryWithUser>>,
    onStoryClick: (String) -> Unit,
    onAddStoryClick: () -> Unit
) {
    val colors = HabitateTheme.colors
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.lg),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onAddStoryClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(Size.avatarLg)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .border(2.dp, colors.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Moment",
                        tint = colors.primary
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Your Moment",
                    style = CaptionText,
                    color = colors.textSecondary
                )
            }
        }

        items(groupedStories.keys.toList()) { userId ->
            val stories = groupedStories[userId] ?: return@items
            if (stories.isEmpty()) return@items
            val user = stories.firstOrNull()?.user ?: return@items
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryClick(userId) }
            ) {
                Box(
                    modifier = Modifier
                        .size(Size.avatarLg)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .border(2.dp, colors.primary, CircleShape)
                ) {
                    user?.avatarUrl?.let { avatarUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.padding(Spacing.lg)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = user?.username ?: "User",
                    style = CaptionText,
                    color = colors.textSecondary
                )
            }
        }
    }
}
