package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - STATES                   ║
 * ║                                                                          ║
 * ║  Empty, loading, and error state components                              ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Empty state with icon, title, description, and optional action.
 */
@Composable
fun HabitateEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(Size.iconXxl),
                tint = colors.primary
            )
        }
        
        Spacer(Modifier.height(Spacing.xxl))
        
        Text(
            text = title,
            style = SectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = SupportingText,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xxl))
            HabitatePrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}

/**
 * Compact empty state for inline use.
 */
@Composable
fun HabitateEmptyStateCompact(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox
) {
    val colors = HabitateTheme.colors
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Size.iconMd),
            tint = colors.textMuted
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = message,
            style = SupportingText,
            color = colors.textMuted
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ERROR STATE
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Error state with retry option.
 */
@Composable
fun HabitateErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    retryText: String = "Try Again",
    onRetry: (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconXxl),
                tint = colors.error
            )
        }
        
        Spacer(Modifier.height(Spacing.xxl))
        
        Text(
            text = title,
            style = SectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = SupportingText,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(Modifier.height(Spacing.xxl))
            HabitateSecondaryButton(
                text = retryText,
                onClick = onRetry
            )
        }
    }
}

/**
 * Network error state.
 */
@Composable
fun HabitateNetworkErrorState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    HabitateErrorState(
        title = "No Connection",
        description = "Please check your internet connection and try again.",
        modifier = modifier,
        icon = Icons.Outlined.CloudOff,
        onRetry = onRetry
    )
}

/**
 * Search empty state.
 */
@Composable
fun HabitateSearchEmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    HabitateEmptyState(
        title = "No Results",
        description = "We couldn't find anything matching \"$query\". Try different keywords.",
        modifier = modifier,
        icon = Icons.Outlined.SearchOff
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// LOADING STATE - SHIMMER SKELETON
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Shimmer effect using logo gradient colors.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(Radius.sm)
) {
    val colors = HabitateTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerTranslate by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Duration.shimmer,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    val shimmerColors = if (colors.isDark) {
        listOf(
            NeutralDark300,
            NeutralDark400,
            NeutralDark300
        )
    } else {
        listOf(
            Neutral200,
            Neutral300,
            Neutral200
        )
    }
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerTranslate, 0f),
        end = Offset(shimmerTranslate + 500f, 0f)
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Skeleton card for loading states.
 */
@Composable
fun HabitateSkeletonCard(
    modifier: Modifier = Modifier
) {
    HabitateCard(
        modifier = modifier.fillMaxWidth(),
        elevation = Elevation.none
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            ShimmerBox(
                modifier = Modifier.size(Size.avatarLg),
                shape = RoundedCornerShape(Radius.pill)
            )
            
            Spacer(Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                // Title placeholder
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                )
                
                Spacer(Modifier.height(Spacing.sm))
                
                // Subtitle placeholder
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                )
            }
        }
    }
}

/**
 * Skeleton list for loading states.
 */
@Composable
fun HabitateSkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        repeat(itemCount) {
            HabitateSkeletonCard()
        }
    }
}

/**
 * Skeleton post card for feed loading.
 */
@Composable
fun HabitateSkeletonPost(
    modifier: Modifier = Modifier
) {
    HabitateCard(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(
                modifier = Modifier.size(Size.avatarMd),
                shape = RoundedCornerShape(Radius.pill)
            )
            Spacer(Modifier.width(Spacing.md))
            Column {
                ShimmerBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                )
                Spacer(Modifier.height(Spacing.xs))
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                )
            }
        }
        
        Spacer(Modifier.height(Spacing.lg))
        
        // Content lines
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        )
        Spacer(Modifier.height(Spacing.sm))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(14.dp)
        )
        Spacer(Modifier.height(Spacing.sm))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(14.dp)
        )
        
        Spacer(Modifier.height(Spacing.lg))
        
        // Image placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(Radius.md)
        )
        
        Spacer(Modifier.height(Spacing.lg))
        
        // Actions
        Row {
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
            )
            Spacer(Modifier.width(Spacing.lg))
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LOADING INDICATOR
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Centered loading indicator.
 */
@Composable
fun HabitateLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val colors = HabitateTheme.colors
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = colors.primary,
            strokeWidth = 3.dp
        )
    }
}

/**
 * Full screen loading state.
 */
@Composable
fun HabitateLoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val colors = HabitateTheme.colors
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colors.primary,
                strokeWidth = 4.dp
            )
            
            if (message != null) {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = message,
                    style = SupportingText,
                    color = colors.textSecondary
                )
            }
        }
    }
}
