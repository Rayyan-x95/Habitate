package com.ninety5.habitate.ui.screens.activity

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.components.UserAvatar
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.ShimmerBox
import com.ninety5.habitate.ui.components.designsystem.ShimmerLine
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onNotificationClick: (type: String, id: String) -> Unit,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val colors = HabitateTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ActivitySkeleton()
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.notifications.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            HabitateEmptyState(
                icon = Icons.Rounded.Notifications,
                title = "All Caught Up",
                description = "You have no new notifications.",
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.notifications.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = Spacing.sm,
                    bottom = 96.dp
                )
            ) {
                items(uiState.notifications, key = { it.id }) { notification ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.markAsRead(notification.id)
                                false
                            } else {
                                false
                            }
                        }
                    )

                    LaunchedEffect(notification.isRead) {
                        if (notification.isRead && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            SwipeBackground(isRead = notification.isRead)
                        },
                        content = {
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.id)
                                    onNotificationClick(notification.type, notification.targetId ?: "")
                                }
                            )
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationUiModel,
    onClick: () -> Unit
) {
    val colors = HabitateTheme.colors
    val backgroundColor by animateColorAsState(
        targetValue = if (notification.isRead) {
            colors.background
        } else {
            colors.primary.copy(alpha = 0.06f)
        },
        animationSpec = tween(500),
        label = "notif_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            avatarUrl = null,
            name = notification.title,
            size = Size.avatarMd
        )
        Spacer(Modifier.width(Spacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = notification.message,
                style = HabitateTheme.typography.bodyLarge,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = colors.onBackground
            )
            Spacer(Modifier.height(Spacing.xxs))
            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString(),
                style = HabitateTheme.typography.bodySmall,
                color = colors.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SwipeBackground(isRead: Boolean) {
    val colors = HabitateTheme.colors
    val color = if (isRead) Color.Transparent else colors.primary.copy(alpha = 0.1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = Spacing.xl),
        contentAlignment = Alignment.CenterStart
    ) {
        if (!isRead) {
            Icon(
                Icons.Rounded.Archive,
                contentDescription = "Mark as read",
                tint = colors.primary
            )
        }
    }
}

@Composable
private fun ActivitySkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Spacing.lg)
    ) {
        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier.size(Size.avatarMd),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                Spacer(Modifier.width(Spacing.lg))
                Column(Modifier.weight(1f)) {
                    ShimmerLine(modifier = Modifier.fillMaxWidth(0.8f))
                    Spacer(Modifier.height(Spacing.xs))
                    ShimmerLine(modifier = Modifier.fillMaxWidth(0.4f))
                }
            }
        }
    }
}


