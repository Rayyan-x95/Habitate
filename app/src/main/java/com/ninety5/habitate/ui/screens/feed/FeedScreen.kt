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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import com.ninety5.habitate.domain.model.Story
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ninety5.habitate.ui.common.LocalSnackbarHostState
import com.ninety5.habitate.ui.components.PostItem
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateErrorState
import com.ninety5.habitate.ui.components.designsystem.HabitateLargeTopBar
import com.ninety5.habitate.ui.components.designsystem.ShimmerBox
import com.ninety5.habitate.ui.components.designsystem.ShimmerLine
import com.ninety5.habitate.ui.theme.*
import com.ninety5.habitate.ui.screens.story.StoryViewModel
import com.ninety5.habitate.ui.viewmodel.FeatureFlagsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onChatClick: () -> Unit,
    onStoryClick: (String) -> Unit,
    onAddStoryClick: () -> Unit = {},
    hasNotifications: Boolean = false,
    viewModel: FeedViewModel = hiltViewModel(),
    storyViewModel: StoryViewModel = hiltViewModel(),
    featureFlagsViewModel: FeatureFlagsViewModel = hiltViewModel()
) {
    val colors = HabitateTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val stories by storyViewModel.activeStories.collectAsState()
    val posts = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val context = LocalContext.current
    val featureFlags = featureFlagsViewModel.featureFlags
    val showStories = featureFlags.isStoriesEnabled && stories.isNotEmpty()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = colors.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HabitateLargeTopBar(
                title = "Habitate",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onChatClick) {
                        Icon(
                            imageVector = Icons.Rounded.Chat,
                            contentDescription = "Messages",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(Size.iconMd)
                        )
                    }
                    Box {
                        IconButton(onClick = onNotificationClick) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(Size.iconMd)
                            )
                        }
                        if (hasNotifications) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 8.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                posts.loadState.refresh is LoadState.Loading -> {
                    FeedSkeletonLoader()
                }

                posts.loadState.refresh is LoadState.Error -> {
                    val error = (posts.loadState.refresh as LoadState.Error).error
                    HabitateErrorState(
                        title = "Couldn't load feed",
                        description = error.localizedMessage ?: "Something went wrong",
                        onRetry = { posts.retry() }
                    )
                }

                posts.loadState.refresh is LoadState.NotLoading && posts.itemCount == 0 -> {
                    EmptyFeedState(onCreatePostClick = onCreatePostClick)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        // Stories / Trending bar
                        item(key = "stories_bar") {
                            Column(
                                modifier = Modifier.padding(
                                    top = Spacing.sm,
                                    bottom = Spacing.md
                                )
                            ) {
                                Text(
                                    text = "Stories",
                                    style = SectionTitle,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.screenHorizontal,
                                        vertical = Spacing.xs
                                    )
                                )

                                if (showStories) {
                                    StoriesBar(
                                        stories = stories,
                                        onStoryClick = onStoryClick,
                                        onAddStoryClick = onAddStoryClick
                                    )
                                } else {
                                    StoriesPlaceholderBar(onAddStoryClick = onAddStoryClick)
                                }
                            }
                        }

                        // Feed posts
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
                                    onReactionClick = { reaction ->
                                        viewModel.toggleLike(post.id, reaction)
                                    },
                                    onCommentClick = { onPostClick(post.id) },
                                    onShareClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Check out this post by ${post.authorName}: ${post.contentText}"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            Intent.createChooser(sendIntent, null)
                                        )
                                    },
                                    onUserClick = { onUserClick(post.authorId) }
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                        }

                        // Append loading indicator
                        if (posts.loadState.append is LoadState.Loading) {
                            item(key = "append_loading") {
                                FeedPostSkeleton(
                                    modifier = Modifier.padding(Spacing.screenHorizontal)
                                )
                            }
                        }
                    }
                }
            }

            // Snackbar events
            val snackbarHostState = LocalSnackbarHostState.current
            LaunchedEffect(uiState.error) {
                uiState.error?.let { error ->
                    snackbarHostState.showSnackbar(error)
                    viewModel.clearError()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// EMPTY FEED STATE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun EmptyFeedState(onCreatePostClick: () -> Unit) {
    HabitateEmptyState(
        title = "Your Feed is Empty",
        description = "Follow people or join habitats to see posts here.\nOr be the first to share something!",
        icon = Icons.Rounded.PostAdd,
        actionText = "Create First Post",
        onAction = onCreatePostClick
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// SKELETON LOADERS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun FeedSkeletonLoader() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Stories skeleton
        item(key = "stories_skeleton") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(6) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerBox(
                            modifier = Modifier.size(Size.storyRingSize),
                            shape = RoundedCornerShape(Radius.pill)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        ShimmerLine(width = 0.6f, height = 10.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Post skeletons
        items(4) {
            FeedPostSkeleton()
        }
    }
}

@Composable
fun FeedPostSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(
                modifier = Modifier.size(Size.avatarMd),
                shape = RoundedCornerShape(Radius.pill)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerLine(width = 0.45f, height = 14.dp)
                Spacer(modifier = Modifier.height(Spacing.xs))
                ShimmerLine(width = 0.3f, height = 10.dp)
            }
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        ShimmerLine(width = 1f, height = 14.dp)
        Spacer(modifier = Modifier.height(Spacing.xs))
        ShimmerLine(width = 0.85f, height = 14.dp)
        Spacer(modifier = Modifier.height(Spacing.xs))
        ShimmerLine(width = 0.6f, height = 14.dp)
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            ShimmerBox(
                modifier = Modifier.size(width = 48.dp, height = 20.dp),
                shape = RoundedCornerShape(Radius.xs)
            )
            ShimmerBox(
                modifier = Modifier.size(width = 48.dp, height = 20.dp),
                shape = RoundedCornerShape(Radius.xs)
            )
            ShimmerBox(
                modifier = Modifier.size(width = 48.dp, height = 20.dp),
                shape = RoundedCornerShape(Radius.xs)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STORIES BAR
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun StoriesBar(
    stories: List<Story>,
    onStoryClick: (String) -> Unit,
    onAddStoryClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item(key = "add_story") {
            StoryItem(
                label = "Your Story",
                isAdd = true,
                onClick = onAddStoryClick
            )
        }

        items(stories, key = { it.id }) { story ->
            StoryItem(
                label = story.authorName.ifBlank { "User" },
                hasUnviewed = !story.isViewed,
                onClick = { onStoryClick(story.userId) }
            )
        }
    }
}

@Composable
private fun StoriesPlaceholderBar(onAddStoryClick: () -> Unit) {
    val colors = HabitateTheme.colors

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item(key = "add_story") {
            StoryItem(
                label = "Your Story",
                isAdd = true,
                onClick = onAddStoryClick
            )
        }
        items(4) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(Size.storyRingSize)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .border(Size.storyRingBorder, colors.borderSubtle, CircleShape)
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(colors.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun StoryItem(
    label: String,
    isAdd: Boolean = false,
    hasUnviewed: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colors = HabitateTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(Size.storyRingSize)
                .clip(CircleShape)
                .then(
                    if (hasUnviewed) {
                        Modifier.border(Size.storyRingBorder, colors.primary, CircleShape)
                    } else if (isAdd) {
                        Modifier.border(Size.storyRingBorder, colors.borderSubtle, CircleShape)
                    } else {
                        Modifier.border(Size.storyRingBorder, colors.border, CircleShape)
                    }
                )
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isAdd) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Story",
                    tint = colors.primary,
                    modifier = Modifier.size(Size.iconMd)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = CaptionText,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

