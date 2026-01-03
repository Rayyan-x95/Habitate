package com.ninety5.habitate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - TOKENS                       ║
 * ║                                                                          ║
 * ║  Spacing, sizing, elevation, and structural tokens                       ║
 * ║  Following Material 3 + Apple-inspired minimalism                        ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// SPACING SYSTEM (4dp base unit)
// ═══════════════════════════════════════════════════════════════════════════

object Spacing {
    /** 2dp - Micro spacing (icon-text gap) */
    val xxs: Dp = 2.dp
    
    /** 4dp - Tiny spacing */
    val xs: Dp = 4.dp
    
    /** 8dp - Small spacing */
    val sm: Dp = 8.dp
    
    /** 12dp - Small-medium spacing */
    val md: Dp = 12.dp
    
    /** 16dp - Base spacing (component padding) */
    val lg: Dp = 16.dp
    
    /** 20dp - Medium-large spacing */
    val xl: Dp = 20.dp
    
    /** 24dp - Large spacing (section gaps) */
    val xxl: Dp = 24.dp
    
    /** 32dp - Extra large spacing */
    val xxxl: Dp = 32.dp
    
    /** 40dp - Huge spacing */
    val huge: Dp = 40.dp
    
    /** 48dp - Massive spacing */
    val massive: Dp = 48.dp
    
    /** 64dp - Section divider */
    val section: Dp = 64.dp
    
    // Screen edge padding
    val screenHorizontal: Dp = 20.dp
    val screenVertical: Dp = 16.dp
    
    // Card internal padding
    val cardPadding: Dp = 16.dp
    val cardPaddingLarge: Dp = 20.dp
    
    // List item spacing
    val listItemGap: Dp = 12.dp
    val listSectionGap: Dp = 24.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// CORNER RADIUS SYSTEM
// ═══════════════════════════════════════════════════════════════════════════

object Radius {
    /** 4dp - Micro (tags, tiny chips) */
    val xs: Dp = 4.dp
    
    /** 8dp - Small (buttons, inputs) */
    val sm: Dp = 8.dp
    
    /** 12dp - Medium (cards, dialogs) */
    val md: Dp = 12.dp
    
    /** 16dp - Large (modals, sheets) */
    val lg: Dp = 16.dp
    
    /** 20dp - Extra large (featured cards) */
    val xl: Dp = 20.dp
    
    /** 24dp - Huge (bottom sheets) */
    val xxl: Dp = 24.dp
    
    /** 28dp - Maximum (overlays) */
    val xxxl: Dp = 28.dp
    
    /** Full pill shape */
    val pill: Dp = 100.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// SIZING SYSTEM
// ═══════════════════════════════════════════════════════════════════════════

object Size {
    // Icons
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 28.dp
    val iconXl: Dp = 32.dp
    val iconXxl: Dp = 40.dp
    val iconHuge: Dp = 48.dp
    
    // Avatars
    val avatarXs: Dp = 24.dp
    val avatarSm: Dp = 32.dp
    val avatarMd: Dp = 40.dp
    val avatarLg: Dp = 48.dp
    val avatarXl: Dp = 56.dp
    val avatarXxl: Dp = 72.dp
    val avatarHuge: Dp = 96.dp
    
    // Touch targets (WCAG minimum 44dp)
    val touchTarget: Dp = 44.dp
    val touchTargetLarge: Dp = 48.dp
    
    // Buttons
    val buttonHeightSm: Dp = 36.dp
    val buttonHeightMd: Dp = 44.dp
    val buttonHeightLg: Dp = 52.dp
    val buttonHeightXl: Dp = 56.dp
    
    // FAB
    val fabSmall: Dp = 40.dp
    val fabMedium: Dp = 56.dp
    val fabLarge: Dp = 96.dp
    
    // Navigation
    val bottomNavHeight: Dp = 80.dp
    val topAppBarHeight: Dp = 64.dp
    
    // Cards
    val cardMinHeight: Dp = 80.dp
    val cardImageHeight: Dp = 200.dp
    
    // Inputs
    val inputHeight: Dp = 56.dp
    val inputHeightSmall: Dp = 44.dp
    
    // Dividers
    val dividerThickness: Dp = 1.dp
    val dividerThicknessBold: Dp = 2.dp
    
    // Borders
    val borderThin: Dp = 1.dp
    val borderMedium: Dp = 1.5.dp
    val borderThick: Dp = 2.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// ELEVATION SYSTEM (Subtle, no harsh shadows)
// ═══════════════════════════════════════════════════════════════════════════

object Elevation {
    /** No elevation */
    val none: Dp = 0.dp
    
    /** 1dp - Subtle lift (cards) */
    val xs: Dp = 1.dp
    
    /** 2dp - Low elevation (buttons) */
    val sm: Dp = 2.dp
    
    /** 4dp - Medium elevation (FAB) */
    val md: Dp = 4.dp
    
    /** 6dp - High elevation (dialogs) */
    val lg: Dp = 6.dp
    
    /** 8dp - Highest (modals) */
    val xl: Dp = 8.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// GLASSMORPHISM TOKENS
// ═══════════════════════════════════════════════════════════════════════════

object GlassTokens {
    /** Blur radius for glass effect */
    val blur: Dp = 16.dp
    val blurStrong: Dp = 24.dp
    val blurSubtle: Dp = 12.dp
    
    /** Background opacity (8-16% range) */
    const val backgroundAlpha: Float = 0.12f
    const val backgroundAlphaLight: Float = 0.08f
    const val backgroundAlphaStrong: Float = 0.16f
    
    /** Border opacity (10-15% range) */
    const val borderAlpha: Float = 0.12f
    const val borderAlphaLight: Float = 0.08f
    const val borderAlphaStrong: Float = 0.18f
    
    /** Highlight opacity */
    const val highlightAlpha: Float = 0.15f
    
    /** Border width */
    val borderWidth: Dp = 1.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// DESIGN TOKENS COMPOSITION LOCAL
// ═══════════════════════════════════════════════════════════════════════════

data class HabitateTokens(
    val spacing: SpacingTokens,
    val radius: RadiusTokens,
    val size: SizeTokens,
    val elevation: ElevationTokens,
    val glass: GlassTokensData
)

data class SpacingTokens(
    val xxs: Dp = Spacing.xxs,
    val xs: Dp = Spacing.xs,
    val sm: Dp = Spacing.sm,
    val md: Dp = Spacing.md,
    val lg: Dp = Spacing.lg,
    val xl: Dp = Spacing.xl,
    val xxl: Dp = Spacing.xxl,
    val xxxl: Dp = Spacing.xxxl,
    val screenHorizontal: Dp = Spacing.screenHorizontal,
    val screenVertical: Dp = Spacing.screenVertical,
    val cardPadding: Dp = Spacing.cardPadding
)

data class RadiusTokens(
    val xs: Dp = Radius.xs,
    val sm: Dp = Radius.sm,
    val md: Dp = Radius.md,
    val lg: Dp = Radius.lg,
    val xl: Dp = Radius.xl,
    val xxl: Dp = Radius.xxl,
    val pill: Dp = Radius.pill
)

data class SizeTokens(
    val iconMd: Dp = Size.iconMd,
    val iconLg: Dp = Size.iconLg,
    val avatarMd: Dp = Size.avatarMd,
    val avatarLg: Dp = Size.avatarLg,
    val touchTarget: Dp = Size.touchTarget,
    val buttonHeightMd: Dp = Size.buttonHeightMd,
    val fabMedium: Dp = Size.fabMedium,
    val bottomNavHeight: Dp = Size.bottomNavHeight
)

data class ElevationTokens(
    val none: Dp = Elevation.none,
    val xs: Dp = Elevation.xs,
    val sm: Dp = Elevation.sm,
    val md: Dp = Elevation.md,
    val lg: Dp = Elevation.lg
)

data class GlassTokensData(
    val blur: Dp = GlassTokens.blur,
    val backgroundAlpha: Float = GlassTokens.backgroundAlpha,
    val borderAlpha: Float = GlassTokens.borderAlpha,
    val borderWidth: Dp = GlassTokens.borderWidth
)

val DefaultHabitateTokens = HabitateTokens(
    spacing = SpacingTokens(),
    radius = RadiusTokens(),
    size = SizeTokens(),
    elevation = ElevationTokens(),
    glass = GlassTokensData()
)

val LocalHabitateTokens = staticCompositionLocalOf { DefaultHabitateTokens }

object HabitateDesign {
    val tokens: HabitateTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHabitateTokens.current
}
