package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
 * ║                         Version 2.0 - Minimal Redesign                   ║
 * ║                                                                          ║
 * ║  Design Philosophy:                                                       ║
 * ║  • Calm, non-aggressive button styles                                    ║
 * ║  • Subtle press feedback (no harsh animations)                           ║
 * ║  • Clear visual hierarchy (Primary > Secondary > Ghost)                  ║
 * ║  • Accessible touch targets (44dp minimum)                               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// PRIMARY BUTTON (Main CTA)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Primary CTA button with brand color and subtle press animation.
 * Use for primary actions: "Save", "Create", "Submit"
 */
@Composable
fun HabitatePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Subtle scale animation (98% when pressed)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
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
            defaultElevation = Elevation.xs,
            pressedElevation = Elevation.none,
            disabledElevation = Elevation.none
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Size.iconMd),
                color = colors.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
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
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECONDARY BUTTON (Outlined - Second priority actions)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Secondary outlined button for secondary actions.
 * Use for: "Cancel", "View Details", secondary CTAs
 */
@Composable
fun HabitateSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "buttonScale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(Size.buttonHeightMd)
            .scale(scale),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary,
            disabledContentColor = colors.textDisabled
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (enabled) colors.border else colors.borderSubtle
            )
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
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
}

// ═══════════════════════════════════════════════════════════════════════════
// TEXT BUTTON (Ghost - Minimal visual weight)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Text button for tertiary actions with minimal visual weight.
 * Use for: "Skip", "Learn more", inline actions
 */
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
        ),
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
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
}

// ═══════════════════════════════════════════════════════════════════════════
// TONAL BUTTON (Filled Tonal - Soft emphasis)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Tonal button with soft background for medium emphasis.
 * Use when you need more emphasis than outlined but less than primary.
 */
@Composable
fun HabitateTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = HabitateTheme.colors
    
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(Size.buttonHeightMd),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            disabledContainerColor = colors.primaryContainer.copy(alpha = 0.38f),
            disabledContentColor = colors.onPrimaryContainer.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
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
}

// ═══════════════════════════════════════════════════════════════════════════
// ICON BUTTON (Touch-friendly icon actions)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Icon-only button for actions like close, menu, settings.
 * Ensures 44dp minimum touch target.
 */
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

/**
 * Icon button with subtle background for emphasis.
 */
@Composable
fun HabitateFilledIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val colors = HabitateTheme.colors
    
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(Size.touchTarget),
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor ?: colors.primaryContainer,
            contentColor = contentColor ?: colors.onPrimaryContainer,
            disabledContainerColor = colors.surfaceVariant,
            disabledContentColor = colors.textDisabled
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// FLOATING ACTION BUTTON (Minimal elevation)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * FAB with subtle elevation and calm appearance.
 */
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
                defaultElevation = Elevation.sm,
                pressedElevation = Elevation.xs,
                hoveredElevation = Elevation.md
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
                defaultElevation = Elevation.sm,
                pressedElevation = Elevation.xs,
                hoveredElevation = Elevation.md
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

/**
 * Small FAB variant.
 */
@Composable
fun HabitateSmallFab(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color? = null
) {
    val colors = HabitateTheme.colors
    
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor ?: colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        shape = FabShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Elevation.xs,
            pressedElevation = Elevation.none
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CHIP / TAG (Filter chips, selection chips)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Chip for filtering and selection with smooth state transitions.
 */
@Composable
fun HabitateChip(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val colors = HabitateTheme.colors
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.chipBackground,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "chipBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.textSecondary,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "chipContent"
    )
    
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = ChipShape,
        color = backgroundColor,
        tonalElevation = if (selected) Elevation.xs else Elevation.none
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
            if (trailingIcon != null) {
                Spacer(Modifier.width(Spacing.xs))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(Size.iconXs),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Input chip for user-entered items (can be removed).
 */
@Composable
fun HabitateInputChip(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val colors = HabitateTheme.colors
    
    InputChip(
        selected = false,
        onClick = { },
        label = { Text(text, style = MetaText) },
        modifier = modifier,
        shape = ChipShape,
        colors = InputChipDefaults.inputChipColors(
            containerColor = colors.surfaceVariant,
            labelColor = colors.textSecondary
        ),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(Size.iconSm),
                    tint = colors.textMuted
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(Size.touchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(Size.iconXs),
                    tint = colors.textMuted
                )
            }
        },
        border = null
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// SEGMENTED BUTTON (Tab-like selection)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Segmented control for mutually exclusive options with smooth transitions.
 */
@Composable
fun HabitateSegmentedButtons(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector>? = null
) {
    val colors = HabitateTheme.colors
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.chipBackground)
            .padding(Spacing.xxs)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) colors.surface else Color.Transparent,
                animationSpec = tween(durationMillis = Duration.fast),
                label = "segmentBackground"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) colors.textPrimary else colors.textMuted,
                animationSpec = tween(durationMillis = Duration.fast),
                label = "segmentContent"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(backgroundColor)
                    .clickable { onSelectionChanged(index) }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.md),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    icons?.getOrNull(index)?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(Size.iconSm),
                            tint = contentColor
                        )
                        Spacer(Modifier.width(Spacing.xs))
                    }
                    Text(
                        text = option,
                        style = MetaText,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Toggle button for binary choices (on/off, yes/no).
 */
@Composable
fun HabitateToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedIcon: ImageVector? = null,
    uncheckedIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = HabitateTheme.colors
    
    val containerColor by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.surfaceVariant,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "toggleContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) colors.onPrimary else colors.textMuted,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "toggleContent"
    )
    
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.size(Size.touchTarget),
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            checkedContainerColor = colors.primary,
            checkedContentColor = colors.onPrimary,
            disabledContainerColor = colors.surfaceVariant,
            disabledContentColor = colors.textDisabled
        )
    ) {
        val icon = if (checked) checkedIcon else uncheckedIcon
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(Size.iconMd)
            )
        }
    }
}
