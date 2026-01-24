package com.ninety5.habitate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - TOKENS                       ║
 * ║                         Version 2.0 - Minimal Redesign                   ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Breathable layouts with generous whitespace                           ║
 * ║  • Subtle elevation (1-2dp max for cards)                                ║
 * ║  • Consistent touch targets (44dp minimum)                               ║
 * ║  • Purposeful spacing that creates visual calm                           ║
 * ║                                                                          ║
 * ║  Inspiration: Apple Health, Notion, Linear, Calm                         ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// SPACING SYSTEM (4dp base unit - Refined for breathing room)
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
    
    // Screen edge padding (generous for premium feel)
    val screenHorizontal: Dp = 20.dp
    val screenVertical: Dp = 16.dp
    val screenPaddingLarge: Dp = 24.dp
    
    // Card internal padding
    val cardPadding: Dp = 16.dp
    val cardPaddingLarge: Dp = 20.dp
    val cardPaddingCompact: Dp = 12.dp
    
    // List item spacing
    val listItemGap: Dp = 12.dp
    val listSectionGap: Dp = 24.dp
    val listItemPaddingVertical: Dp = 14.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// CORNER RADIUS SYSTEM (Refined for minimal, premium feel)
// ═══════════════════════════════════════════════════════════════════════════

object Radius {
    /** 4dp - Micro (tiny chips, progress indicators) */
    val xs: Dp = 4.dp
    
    /** 8dp - Small (standard buttons, inputs) */
    val sm: Dp = 8.dp
    
    /** 12dp - Medium (cards, dialogs) */
    val md: Dp = 12.dp
    
    /** 16dp - Large (featured cards, modals) */
    val lg: Dp = 16.dp
    
    /** 20dp - Extra large (hero cards) */
    val xl: Dp = 20.dp
    
    /** 24dp - Huge (bottom sheets top corners) */
    val xxl: Dp = 24.dp
    
    /** 28dp - Maximum (full-screen overlays) */
    val xxxl: Dp = 28.dp
    
    /** Full pill shape - Use for pills, tags, FABs */
    val pill: Dp = 100.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// SIZING SYSTEM (Touch-friendly, accessible)
// ═══════════════════════════════════════════════════════════════════════════

object Size {
    // Icons (Material Design standard + custom)
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp      // Material default
    val iconLg: Dp = 28.dp
    val iconXl: Dp = 32.dp
    val iconXxl: Dp = 40.dp
    val iconHuge: Dp = 48.dp
    val iconHero: Dp = 64.dp    // For empty states, hero sections
    
    // Avatars
    val avatarXs: Dp = 24.dp    // Inline mentions
    val avatarSm: Dp = 32.dp    // List items (compact)
    val avatarMd: Dp = 40.dp    // List items (standard)
    val avatarLg: Dp = 48.dp    // Card headers
    val avatarXl: Dp = 56.dp    // Profile cards
    val avatarXxl: Dp = 72.dp   // Profile sections
    val avatarHuge: Dp = 96.dp  // Profile headers
    val avatarHero: Dp = 120.dp // Profile page hero
    
    // Touch targets (WCAG minimum 44dp)
    val touchTarget: Dp = 44.dp
    val touchTargetLarge: Dp = 48.dp
    
    // Buttons (refined heights)
    val buttonHeightSm: Dp = 36.dp    // Compact/inline buttons
    val buttonHeightMd: Dp = 44.dp    // Standard buttons (touch-friendly)
    val buttonHeightLg: Dp = 52.dp    // Primary CTAs
    val buttonHeightXl: Dp = 56.dp    // Hero CTAs
    
    // FAB
    val fabSmall: Dp = 40.dp
    val fabMedium: Dp = 56.dp
    val fabLarge: Dp = 96.dp
    
    // Navigation
    val bottomNavHeight: Dp = 72.dp   // Slightly reduced for cleaner look
    val topAppBarHeight: Dp = 56.dp   // Standard
    val topAppBarHeightLarge: Dp = 64.dp
    
    // Cards
    val cardMinHeight: Dp = 72.dp
    val cardImageHeight: Dp = 200.dp
    val cardImageHeightCompact: Dp = 160.dp
    
    // Inputs
    val inputHeight: Dp = 52.dp       // Comfortable touch target
    val inputHeightSmall: Dp = 44.dp
    
    // Dividers
    val dividerThickness: Dp = 0.5.dp  // Subtle dividers
    val dividerThicknessBold: Dp = 1.dp
    
    // Borders
    val borderThin: Dp = 0.5.dp
    val borderMedium: Dp = 1.dp
    val borderThick: Dp = 1.5.dp
    
    // Story ring
    val storyRingSize: Dp = 68.dp
    val storyRingBorder: Dp = 2.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// ELEVATION SYSTEM (Minimal - 1-2dp max for calm aesthetic)
// ═══════════════════════════════════════════════════════════════════════════

object Elevation {
    /** No elevation - Default for most surfaces */
    val none: Dp = 0.dp
    
    /** 0.5dp - Whisper elevation (subtle lift) */
    val whisper: Dp = 0.5.dp
    
    /** 1dp - Subtle lift (cards, resting state) */
    val xs: Dp = 1.dp
    
    /** 2dp - Low elevation (pressed buttons, floating elements) */
    val sm: Dp = 2.dp
    
    /** 3dp - Medium elevation (FAB resting) */
    val md: Dp = 3.dp
    
    /** 4dp - High elevation (dialogs, menus) */
    val lg: Dp = 4.dp
    
    /** 6dp - Highest (modals, navigation drawers) */
    val xl: Dp = 6.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// GLASSMORPHISM TOKENS (Subtle, non-distracting)
// ═══════════════════════════════════════════════════════════════════════════

object GlassTokens {
    /** Blur radius for glass effect */
    val blur: Dp = 16.dp
    val blurStrong: Dp = 24.dp
    val blurSubtle: Dp = 10.dp
    
    /** Background opacity (reduced for subtlety) */
    const val backgroundAlpha: Float = 0.08f
    const val backgroundAlphaLight: Float = 0.05f
    const val backgroundAlphaStrong: Float = 0.12f
    
    /** Border opacity (very subtle) */
    const val borderAlpha: Float = 0.08f
    const val borderAlphaLight: Float = 0.05f
    const val borderAlphaStrong: Float = 0.12f
    
    /** Highlight opacity */
    const val highlightAlpha: Float = 0.08f
    
    /** Border width */
    val borderWidth: Dp = 0.5.dp
}

// ═══════════════════════════════════════════════════════════════════════════
// MOTION TOKENS - See Motion.kt for full implementation
// Duration and easing tokens are defined in Motion.kt (Duration, Easing objects)
// ═══════════════════════════════════════════════════════════════════════════

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
    val cardPadding: Dp = Spacing.cardPadding,
    val screenPaddingLarge: Dp = Spacing.screenPaddingLarge,
    val cardPaddingCompact: Dp = Spacing.cardPaddingCompact,
    val listItemPaddingVertical: Dp = Spacing.listItemPaddingVertical
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
    val whisper: Dp = Elevation.whisper,
    val xs: Dp = Elevation.xs,
    val sm: Dp = Elevation.sm,
    val md: Dp = Elevation.md,
    val lg: Dp = Elevation.lg,
    val xl: Dp = Elevation.xl
)

data class GlassTokensData(
    val blur: Dp = GlassTokens.blur,
    val blurSubtle: Dp = GlassTokens.blurSubtle,
    val backgroundAlpha: Float = GlassTokens.backgroundAlpha,
    val backgroundAlphaLight: Float = GlassTokens.backgroundAlphaLight,
    val backgroundAlphaStrong: Float = GlassTokens.backgroundAlphaStrong,
    val borderAlpha: Float = GlassTokens.borderAlpha,
    val borderAlphaLight: Float = GlassTokens.borderAlphaLight,
    val borderAlphaStrong: Float = GlassTokens.borderAlphaStrong,
    val highlightAlpha: Float = GlassTokens.highlightAlpha,
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
