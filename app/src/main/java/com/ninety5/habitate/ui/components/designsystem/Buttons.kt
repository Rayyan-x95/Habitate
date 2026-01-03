package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - BUTTONS                  ║
 * ║                                                                          ║
 * ║  Premium, calm button components following the design system             ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// PRIMARY BUTTON
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Primary CTA button with brand gradient and press animation.
 */
@Composable
fun HabitatePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = Motion.buttonPress(),
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(Size.buttonHeightLg)
            .scale(scale),
        enabled = enabled && !loading,
        shape = PrimaryButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = 0.38f),
            disabledContentColor = colors.onPrimary.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Elevation.sm,
            pressedElevation = Elevation.xs
        ),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Size.iconMd),
                color = colors.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Size.iconMd)
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = ButtonText
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECONDARY BUTTON (Outlined)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = Motion.buttonPress(),
        label = "buttonScale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(Size.buttonHeightMd)
            .scale(scale),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary,
            disabledContentColor = colors.textDisabled
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled),
        interactionSource = interactionSource
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconSm)
            )
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            style = ButtonText
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TEXT BUTTON (Minimal)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    color: Color? = null
) {
    val colors = HabitateTheme.colors
    val contentColor = color ?: colors.primary
    
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = colors.textDisabled
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.iconSm)
            )
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(
            text = text,
            style = ButtonText
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ICON BUTTON
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tint: Color? = null,
    size: Dp = Size.touchTarget
) {
    val colors = HabitateTheme.colors
    val iconTint = tint ?: colors.textSecondary
    
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) iconTint else colors.textDisabled,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// FLOATING ACTION BUTTON
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateFab(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    extended: Boolean = false,
    text: String? = null
) {
    val colors = HabitateTheme.colors
    
    if (extended && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = colors.fabBackground,
            contentColor = colors.fabContent,
            shape = ExtendedFabShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = Elevation.md,
                pressedElevation = Elevation.sm
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Size.iconMd)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(text = text, style = ButtonText)
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.size(Size.fabMedium),
            containerColor = colors.fabBackground,
            contentColor = colors.fabContent,
            shape = FabShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = Elevation.md,
                pressedElevation = Elevation.sm
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Size.iconLg)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CHIP / TAG
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateChip(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = HabitateTheme.colors
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.chipBackground,
        label = "chipBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.textSecondary,
        label = "chipContent"
    )
    
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = ChipShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Size.iconSm),
                    tint = contentColor
                )
                Spacer(Modifier.width(Spacing.xs))
            }
            Text(
                text = text,
                style = MetaText,
                color = contentColor
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SEGMENTED BUTTON
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateSegmentedButtons(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(colors.chipBackground)
            .padding(Spacing.xxs)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) colors.surface else Color.Transparent,
                label = "segmentBackground"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.xs))
                    .background(backgroundColor)
                    .clickable { onSelectionChanged(index) }
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MetaText,
                    color = if (isSelected) colors.textPrimary else colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
