package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - CARDS                    ║
 * ║                                                                          ║
 * ║  Premium card components with subtle elevation and glass effects         ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// STANDARD CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard card with subtle elevation.
 */
@Composable
fun HabitateCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = Elevation.xs,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = Elevation.none
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// FEATURED CARD (Highlighted)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Featured card with accent border.
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
    
    Card(
        modifier = modifier
            .border(
                width = Size.borderMedium,
                color = accent.copy(alpha = 0.5f),
                shape = FeaturedCardShape
            ),
        shape = FeaturedCardShape,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPaddingLarge),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GLASS CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with glassmorphism effect.
 */
@Composable
fun HabitateGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    
    GlassContainer(
        modifier = modifier,
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
// OUTLINED CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with subtle border, no elevation.
 */
@Composable
fun HabitateOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    
    OutlinedCard(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = colors.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENT CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card with brand gradient background.
 */
@Composable
fun HabitateGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = GradientBrand,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(FeaturedCardShape)
            .background(gradient)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPaddingLarge),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STAT CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Compact card for displaying statistics.
 */
@Composable
fun HabitateStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trend: String? = null,
    trendPositive: Boolean = true
) {
    val colors = HabitateTheme.colors
    
    HabitateCard(
        modifier = modifier,
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
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
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
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LIST ITEM CARD
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Card styled as a list item with leading, content, and trailing.
 */
@Composable
fun HabitateListCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    HabitateCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = Elevation.none
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
}
