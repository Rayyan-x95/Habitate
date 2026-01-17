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
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - STATES                   ║
 * ║                                                                          ║
 * ║  Calm, reassuring empty, loading, and error state components             ║
 * ║  Design principle: Non-alarming, helpful, and actionable                 ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// EMPTY STATE (Encouraging, not discouraging)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Empty state with icon, title, description, and optional action.
 * Designed to feel encouraging, not alarming.
 */
@Composable
fun HabitateEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    iconTint: Color? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Soft icon container
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconXl),
                tint = iconTint ?: colors.primary.copy(alpha = 0.8f)
            )
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = title,
            style = SectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = BodyText,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xl))
            HabitatePrimaryButton(
                text = actionText,
                onClick = onAction
            )
            
            if (secondaryActionText != null && onSecondaryAction != null) {
                Spacer(Modifier.height(Spacing.sm))
                HabitateTextButton(
                    text = secondaryActionText,
                    onClick = onSecondaryAction
                )
            }
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
// ERROR STATE (Calming, not alarming)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Error state with helpful messaging and retry option.
 * Designed to be calming rather than alarming.
 */
@Composable
fun HabitateErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    retryText: String = "Try Again",
    onRetry: (() -> Unit)? = null,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Soft error container (not aggressive red)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.errorContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconXl),
                tint = colors.error.copy(alpha = 0.8f)
            )
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = title,
            style = SectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = BodyText,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        
        if (onRetry != null) {
            Spacer(Modifier.height(Spacing.xl))
            HabitateSecondaryButton(
                text = retryText,
                onClick = onRetry
            )
            
            if (secondaryText != null && onSecondary != null) {
                Spacer(Modifier.height(Spacing.sm))
                HabitateTextButton(
                    text = secondaryText,
                    onClick = onSecondary
                )
            }
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
// LOADING STATE - SHIMMER SKELETON (Subtle, calming)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Shimmer effect with subtle, calming animation.
 * Uses brand-derived colors for cohesive feel.
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
    
    // Softer shimmer colors that don't draw too much attention
    val shimmerColors = if (colors.isDark) {
        listOf(
            NeutralDark300.copy(alpha = 0.4f),
            NeutralDark400.copy(alpha = 0.6f),
            NeutralDark300.copy(alpha = 0.4f)
        )
    } else {
        listOf(
            Neutral200.copy(alpha = 0.5f),
            Neutral300.copy(alpha = 0.7f),
            Neutral200.copy(alpha = 0.5f)
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
 * Text-shaped shimmer for line placeholders.
 */
@Composable
fun ShimmerLine(
    modifier: Modifier = Modifier,
    width: Float = 1f,
    height: Dp = 14.dp
) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height),
        shape = RoundedCornerShape(Radius.xs)
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
// LOADING INDICATOR (Minimal, unobtrusive)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Centered loading indicator with minimal visual weight.
 */
@Composable
fun HabitateLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    strokeWidth: Dp = 2.5.dp
) {
    val colors = HabitateTheme.colors
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = colors.primary.copy(alpha = 0.8f),
            strokeWidth = strokeWidth,
            trackColor = colors.primaryContainer.copy(alpha = 0.3f)
        )
    }
}

/**
 * Full screen loading state with optional message.
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
                modifier = Modifier.size(44.dp),
                color = colors.primary.copy(alpha = 0.8f),
                strokeWidth = 3.dp,
                trackColor = colors.primaryContainer.copy(alpha = 0.3f)
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

/**
 * Inline loading indicator for buttons or small spaces.
 */
@Composable
fun HabitateInlineLoader(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    color: Color? = null
) {
    val colors = HabitateTheme.colors
    
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color ?: colors.primary,
        strokeWidth = 2.dp
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// SUCCESS STATE (Celebratory but calm)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Success state for completed actions.
 */
@Composable
fun HabitateSuccessState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.CheckCircle,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.successContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconXl),
                tint = colors.success.copy(alpha = 0.9f)
            )
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        Text(
            text = title,
            style = SectionTitle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(Spacing.sm))
        
        Text(
            text = description,
            style = BodyText,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xl))
            HabitatePrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}
