package com.ninety5.habitate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.CardShape
import com.ninety5.habitate.ui.theme.GlassContainer
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Spacing

/**
 * Standard Habitate card with subtle border, no elevation.
 * Uses [CardShape] (20dp corners) and [Spacing.cardPadding] padding by default.
 */
@Composable
fun HabitateCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    borderColor: Color = Color(0xFF2A2A2A),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}

/**
 * Glassmorphism variant of [HabitateCard].
 * Wraps content in a [GlassContainer] for liquid glass effect.
 */
@Composable
fun HabitateGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Box(modifier = cardModifier) {
        GlassContainer(
            shape = shape,
            blur = 16.dp,
            backgroundAlpha = if (HabitateTheme.colors.isDark) 0.1f else 0.4f,
            borderAlpha = 0.1f
        ) {
            Box(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    }
}
