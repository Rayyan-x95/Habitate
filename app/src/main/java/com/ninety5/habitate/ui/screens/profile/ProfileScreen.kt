package com.ninety5.habitate.ui.screens.profile

import com.ninety5.habitate.domain.model.PostVisibility
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.R
import com.ninety5.habitate.ui.components.PostItem
import com.ninety5.habitate.ui.components.UserAvatar
import com.ninety5.habitate.ui.components.designsystem.HabitateBackButton
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateErrorState
import com.ninety5.habitate.ui.components.designsystem.HabitateIconButton
import com.ninety5.habitate.ui.components.designsystem.HabitateLargeTopBar
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.components.designsystem.HabitateTonalButton
import com.ninety5.habitate.ui.components.designsystem.ShimmerBox
import com.ninety5.habitate.ui.components.designsystem.ShimmerLine
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    isViewAsMode: Boolean = false,
    onSettingsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onWorkoutClick: (String) -> Unit,
    onTimelineClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val user = uiState.user
    val colors = HabitateTheme.colors
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val isCurrentUser = if (isViewAsMode) false else uiState.isCurrentUser

    val displayedPosts = remember(uiState.posts, isViewAsMode) {
        if (isViewAsMode) {
            uiState.posts.filter { it.visibility == PostVisibility.PUBLIC }
        } else {
            uiState.posts
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            if (isViewAsMode) {
                HabitateLargeTopBar(
                    title = stringResource(R.string.profile_preview_title),
                    navigationIcon = { HabitateBackButton(onClick = onBackClick) },
                    scrollBehavior = scrollBehavior
                )
            } else {
                HabitateLargeTopBar(
                    title = stringResource(R.string.profile_title),
                    actions = {
                        HabitateIconButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            onClick = onSettingsClick
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                // Loading skeleton
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.isLoading && user == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ProfileSkeleton()
                }

                // Error state
                if (uiState.error != null && user == null) {
                    HabitateErrorState(
                        title = stringResource(R.string.profile_error_title),
                        description = uiState.error ?: stringResource(R.string.profile_error_description),
                        onRetry = { viewModel.retryLoadProfile() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Content
                androidx.compose.animation.AnimatedVisibility(
                    visible = user != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Profile Header
                        item(key = "profile_header") {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg)
                            ) {
                                UserAvatar(
                                    avatarUrl = user?.avatarUrl,
                                    name = user?.displayName,
                                    size = Size.avatarHero
                                )
                                Spacer(modifier = Modifier.height(Spacing.md))
                                Text(
                                    text = user?.displayName ?: "",
                                    style = HabitateTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onBackground
                                )
                                if (!user?.bio.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    Text(
                                        text = user?.bio ?: "",
                                        style = HabitateTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = Spacing.xl)
                                    )
                                }
                            }
                        }

                        // Stats Row
                        item(key = "stats") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStatItem(
                                    label = stringResource(R.string.profile_stat_posts),
                                    value = displayedPosts.size.toString()
                                )
                                ProfileStatItem(
                                    label = stringResource(R.string.profile_stat_followers),
                                    value = uiState.followerCount.toString()
                                )
                                ProfileStatItem(
                                    label = stringResource(R.string.profile_stat_following),
                                    value = uiState.followingCount.toString()
                                )
                            }
                        }

                        // Action Buttons for current user
                        if (isCurrentUser) {
                            item(key = "actions") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = Spacing.screenHorizontal,
                                            vertical = Spacing.md
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    HabitatePrimaryButton(
                                        text = stringResource(R.string.edit_profile),
                                        onClick = onEditProfileClick,
                                        icon = Icons.Rounded.Edit,
                                        modifier = Modifier.weight(1f)
                                    )
                                    HabitateTonalButton(
                                        text = stringResource(R.string.timeline),
                                        onClick = onTimelineClick,
                                        icon = Icons.Default.History,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Divider
                        if (displayedPosts.isNotEmpty()) {
                            item(key = "divider") {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = colors.borderSubtle.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                            }
                        }

                        // Empty posts state
                        if (displayedPosts.isEmpty() && !uiState.isLoading) {
                            item(key = "empty") {
                                HabitateEmptyState(
                                    title = stringResource(R.string.profile_no_posts_title),
                                    description = if (isCurrentUser) stringResource(R.string.profile_no_posts_owner_desc) else stringResource(R.string.profile_no_posts_other_desc),
                                    modifier = Modifier.padding(vertical = Spacing.xxl)
                                )
                            }
                        }

                        // Posts
                        items(displayedPosts, key = { it.id }) { post ->
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onReactionClick = { reaction -> viewModel.toggleLike(post.id, reaction) },
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
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                onUserClick = { /* Already on profile */ }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = colors.borderSubtle.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Profile Stat ----------

@Composable
private fun ProfileStatItem(label: String, value: String) {
    val colors = HabitateTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = Spacing.sm)
    ) {
        Text(
            text = value,
            style = HabitateTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = label,
            style = HabitateTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
    }
}

// ---------- Profile Skeleton ----------

@Composable
private fun ProfileSkeleton() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.screenHorizontal)
            .padding(top = Spacing.xxl)
    ) {
        ShimmerBox(
            modifier = Modifier.size(Size.avatarHero),
            shape = RoundedCornerShape(50)
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        ShimmerLine(modifier = Modifier.width(160.dp))
        Spacer(modifier = Modifier.height(Spacing.xs))
        ShimmerLine(modifier = Modifier.width(220.dp))
        Spacer(modifier = Modifier.height(Spacing.xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ShimmerBox(modifier = Modifier.size(40.dp, 20.dp))
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    ShimmerLine(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}

