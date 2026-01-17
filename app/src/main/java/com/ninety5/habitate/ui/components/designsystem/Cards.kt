package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - CARDS                    ║
 * ║                                                                          ║
 * ║  Premium card components with subtle elevation and calm aesthetics       ║
 * ║  Design principle: Minimal shadows, soft borders, breathable space       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// STANDARD CARD (Primary container)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard card with minimal elevation and soft appearance.
 * Supports optional click interaction with subtle press feedback.
 */
@Composable
fun HabitateCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = Elevation.xs,
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.cardPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    val cardColor = containerColor ?: colors.cardBackground
    
    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val animatedElevation by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isPressed) Elevation.none else elevation,
            animationSpec = tween(durationMillis = Duration.fast),
            label = "cardElevation"
        )
        
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            interactionSource = interactionSource,
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation,
                pressedElevation = Elevation.none
            )
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Compact card variant for denser layouts.
 */
@Composable
fun HabitateCompactCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    HabitateCard(
        modifier = modifier,
        onClick = onClick,
        elevation = Elevation.none,
        contentPadding = PaddingValues(Spacing.cardPaddingCompact),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// FEATURED CARD (Highlighted with accent)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Featured card with subtle accent border for emphasis.
 * Used for promotions, important notices, or highlighted content.
 */
@Composable
fun HabitateFeaturedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    val accent = accentColor ?: colors.accent
    
    val cardModifier = modifier.border(
        width = Size.borderMedium,
        color = accent.copy(alpha = 0.35f),
        shape = FeaturedCardShape
    )
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = FeaturedCardShape,
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Elevation.xs,
                pressedElevation = Elevation.none
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPaddingLarge),
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = FeaturedCardShape,
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.xs)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPaddingLarge),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GLASS CARD (Frosted glass effect)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with subtle glassmorphism effect.
 * Best used over images or gradient backgrounds.
 */
@Composable
fun HabitateGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier
    
    GlassContainer(
        modifier = baseModifier,
        shape = CardShape,
        blur = GlassTokens.blurSubtle,
        backgroundAlpha = GlassTokens.backgroundAlphaLight
    ) {
        Box(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// OUTLINED CARD (Borderline minimal)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with subtle border, no elevation.
 * Ideal for lists or secondary content.
 */
@Composable
fun HabitateOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    val border = borderColor ?: colors.cardBorder
    
    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = colors.surface
            ),
            border = BorderStroke(Size.borderThin, border)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.outlinedCardColors(
                containerColor = colors.surface
            ),
            border = BorderStroke(Size.borderThin, border)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SURFACE CARD (Flat, no elevation)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Simple surface card with no elevation or border.
 * Used for grouping content on elevated surfaces.
 */
@Composable
fun HabitateSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    val cardColor = containerColor ?: colors.surfaceVariant
    
    val cardModifier = modifier
        .clip(CardShape)
        .background(cardColor)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )
    
    Column(
        modifier = cardModifier.padding(Spacing.cardPadding),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENT CARD (Brand emphasis)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with subtle gradient background.
 * Use sparingly for high-priority CTAs or features.
 */
@Composable
fun HabitateGradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradient: Brush = GradientBrandSubtle,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(FeaturedCardShape)
        .background(gradient)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )
    
    Column(
        modifier = baseModifier.padding(Spacing.cardPaddingLarge),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// STAT CARD (Metrics display)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Compact card for displaying statistics and metrics.
 * Supports optional icon, trend indicator, and subtitle.
 */
@Composable
fun HabitateStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    trend: String? = null,
    trendPositive: Boolean = true,
    subtitle: String? = null
) {
    val colors = HabitateTheme.colors
    
    HabitateCard(
        modifier = modifier,
        onClick = onClick,
        elevation = Elevation.none
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(Size.avatarMd)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(colors.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint ?: colors.primary,
                        modifier = Modifier.size(Size.iconMd)
                    )
                }
                Spacer(Modifier.width(Spacing.md))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MetaText,
                    color = colors.textMuted
                )
                Spacer(Modifier.height(Spacing.xxs))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = SectionTitle,
                        color = colors.textPrimary
                    )
                    if (trend != null) {
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = trend,
                            style = MetaText,
                            color = if (trendPositive) colors.success else colors.error
                        )
                    }
                }
                if (subtitle != null) {
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        text = subtitle,
                        style = SupportingText,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Minimal stat display for inline metrics.
 */
@Composable
fun HabitateStatInline(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val colors = HabitateTheme.colors
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = SectionTitle,
            color = valueColor ?: colors.textPrimary
        )
        Spacer(Modifier.height(Spacing.xxs))
        Text(
            text = label,
            style = MetaText,
            color = colors.textMuted
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LIST ITEM CARD (Content rows)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card styled as a list item with leading, content, and trailing.
 * Ideal for settings, menus, or content lists.
 */
@Composable
fun HabitateListCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    val contentAlpha = if (enabled) 1f else 0.5f
    
    HabitateCard(
        modifier = modifier.fillMaxWidth(),
        onClick = if (enabled) onClick else null,
        elevation = Elevation.none
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                Box(modifier = Modifier.alpha(contentAlpha)) {
                    leading()
                }
                Spacer(Modifier.width(Spacing.md))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = CardTitle,
                    color = colors.textPrimary.copy(alpha = contentAlpha)
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        text = subtitle,
                        style = SupportingText,
                        color = colors.textSecondary.copy(alpha = contentAlpha)
                    )
                }
            }
            
            if (trailing != null) {
                Spacer(Modifier.width(Spacing.md))
                Box(modifier = Modifier.alpha(contentAlpha)) {
                    trailing()
                }
            }
        }
    }
}

/**
 * Simple list row without card background.
 */
@Composable
fun HabitateListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(
                horizontal = Spacing.md,
                vertical = Spacing.listItemPaddingVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Spacing.md))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CardTitle,
                color = colors.textPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = subtitle,
                    style = SupportingText,
                    color = colors.textSecondary
                )
            }
        }
        
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.md))
            trailing()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ACTION CARD (CTA with icon)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with prominent action, icon, and optional description.
 */
@Composable
fun HabitateActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconTint: Color? = null,
    containerColor: Color? = null
) {
    val colors = HabitateTheme.colors
    
    HabitateCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = containerColor ?: colors.surfaceVariant.copy(alpha = 0.5f),
        elevation = Elevation.none
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Size.avatarLg)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(colors.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint ?: colors.primary,
                    modifier = Modifier.size(Size.iconLg)
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = CardTitle,
                    color = colors.textPrimary
                )
                if (description != null) {
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        text = description,
                        style = SupportingText,
                        color = colors.textSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(Size.iconMd)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// HELPER EXTENSIONS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun Modifier.alpha(alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer { this.alpha = alpha })
