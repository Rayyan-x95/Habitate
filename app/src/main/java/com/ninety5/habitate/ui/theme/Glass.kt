package com.ninety5.habitate.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - GLASS                        ║
 * ║                                                                          ║
 * ║  Liquid Glass / Glassmorphism (Controlled Usage)                         ║
 * ║                                                                          ║
 * ║  Use sparingly for:                                                       ║
 * ║  • Bottom navigation                                                     ║
 * ║  • Floating cards                                                        ║
 * ║  • Modals & sheets                                                       ║
 * ║                                                                          ║
 * ║  Rules:                                                                   ║
 * ║  • Blur: 12-20dp                                                         ║
 * ║  • Opacity: 8-16%                                                        ║
 * ║  • Border: primary color at 10-15%                                       ║
 * ║  • No heavy shadows                                                      ║
 * ║  • Must feel breathable, not frosted                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// GLASS CONTAINER COMPOSABLE
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Glassmorphism container with subtle blur, tint, and border.
 * 
 * @param modifier Modifier for the container
 * @param shape Shape of the glass container (default: rounded 16dp)
 * @param blur Blur radius (12-20dp recommended)
 * @param backgroundAlpha Background opacity (0.08-0.16 recommended)
 * @param borderAlpha Border opacity (0.10-0.15 recommended)
 * @param content Content inside the glass container
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.lg),
    blur: Dp = GlassTokens.blur,
    backgroundAlpha: Float = GlassTokens.backgroundAlpha,
    borderAlpha: Float = GlassTokens.borderAlpha,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = HabitateTheme.colors
    
    Box(
        modifier = modifier.glassEffect(
            shape = shape,
            blur = blur,
            tintColor = colors.primary,
            backgroundAlpha = backgroundAlpha,
            borderAlpha = borderAlpha,
            isDark = colors.isDark
        ),
        content = content
    )
}

/**
 * Subtle glass for navigation bars - more transparent.
 */
@Composable
fun GlassNavBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl),
        blur = GlassTokens.blurSubtle,
        backgroundAlpha = GlassTokens.backgroundAlphaLight,
        borderAlpha = GlassTokens.borderAlphaLight,
        content = content
    )
}

/**
 * Strong glass for modals and sheets - more prominent.
 */
@Composable
fun GlassModal(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.xxl),
    content: @Composable BoxScope.() -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        blur = GlassTokens.blurStrong,
        backgroundAlpha = GlassTokens.backgroundAlphaStrong,
        borderAlpha = GlassTokens.borderAlphaStrong,
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// GLASS EFFECT MODIFIER
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Applies glass effect to any composable.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(Radius.lg),
    blur: Dp = GlassTokens.blur,
    tintColor: Color = Primary500,
    backgroundAlpha: Float = GlassTokens.backgroundAlpha,
    borderAlpha: Float = GlassTokens.borderAlpha,
    isDark: Boolean = false
): Modifier {
    // Base glass color - slightly different for light/dark
    val glassBase = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }
    
    // Tint using brand primary
    val glassTint = tintColor.copy(alpha = backgroundAlpha)
    
    // Subtle gradient border for highlight effect
    val glassBorder = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.12f else 0.25f),
            tintColor.copy(alpha = borderAlpha)
        )
    )
    
    return this
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.blur(blur)
            } else {
                Modifier
            }
        )
        .clip(shape)
        .background(glassBase, shape)
        .background(glassTint, shape)
        .border(GlassTokens.borderWidth, glassBorder, shape)
}

/**
 * Lighter glass effect for cards and chips.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(Radius.md),
    isDark: Boolean = false
): Modifier = glassEffect(
    shape = shape,
    blur = GlassTokens.blurSubtle,
    backgroundAlpha = GlassTokens.backgroundAlphaLight,
    borderAlpha = GlassTokens.borderAlphaLight,
    isDark = isDark
)

/**
 * Strong glass effect for floating elements.
 */
fun Modifier.glassFloating(
    shape: Shape = RoundedCornerShape(Radius.lg),
    isDark: Boolean = false
): Modifier = glassEffect(
    shape = shape,
    blur = GlassTokens.blurStrong,
    backgroundAlpha = GlassTokens.backgroundAlphaStrong,
    borderAlpha = GlassTokens.borderAlphaStrong,
    isDark = isDark
)

// ═══════════════════════════════════════════════════════════════════════════
// LEGACY SUPPORT
// ═══════════════════════════════════════════════════════════════════════════

// Keep old signature for backward compatibility
@Deprecated("Use new glassEffect with explicit parameters", ReplaceWith("glassEffect()"))
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(16.dp),
    tint: Color = PrimarySoft.copy(alpha = 0.12f),
    blur: Dp = 20.dp
): Modifier = glassEffect(
    shape = shape,
    blur = blur,
    tintColor = Primary500,
    backgroundAlpha = 0.12f,
    borderAlpha = 0.12f,
    isDark = false
)
