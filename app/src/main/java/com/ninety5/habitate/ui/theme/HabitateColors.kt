package com.ninety5.habitate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - COLORS                       ║
 * ║                         Version 2.0 - Minimal Redesign                   ║
 * ║                                                                          ║
 * ║  Design Philosophy:                                                       ║
 * ║  • Quietly powerful, thoughtfully designed, effortless to use           ║
 * ║  • Calm, minimal, human-centric, premium, non-distracting               ║
 * ║                                                                          ║
 * ║  Color Strategy (Logo-derived):                                          ║
 * ║  • Primary: Forest Green (#1F3D32) - Trust, growth, calm                ║
 * ║  • Accent: Warm Amber (#D4A574) - Warmth, highlights                    ║
 * ║  • Neutrals: Desaturated greens (warm undertone)                        ║
 * ║  • Semantics: Muted, non-aggressive tones                               ║
 * ║                                                                          ║
 * ║  Inspiration: Apple Health, Notion, Linear, Calm                         ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// BRAND CORE (From Logo - Refined)
// ═══════════════════════════════════════════════════════════════════════════

/** Primary brand color - Forest Green from logo background */
val BrandPrimary = Color(0xFF1F3D32)

/** Secondary brand - Lighter sage derived from primary */
val BrandSecondary = Color(0xFF3D6B5C)

/** Accent - Warm amber for highlights & interactions (slightly muted) */
val BrandAccent = Color(0xFFCFA06A)

/** Cream - Logo color for contrast elements (warmer) */
val BrandCream = Color(0xFFFAF8F5)

/** Deep Forest - Darkest brand shade */
val BrandDeepForest = Color(0xFF0F1C18)

// ═══════════════════════════════════════════════════════════════════════════
// PRIMARY PALETTE (5 shades from logo green - Refined for calm aesthetic)
// ═══════════════════════════════════════════════════════════════════════════

val Primary50 = Color(0xFFF5F8F7)   // Lightest tint (calmer)
val Primary100 = Color(0xFFE5EEEA)  // Very light
val Primary200 = Color(0xFFC5D9D1)  // Light
val Primary300 = Color(0xFF8BB5A4)  // Medium light (softer)
val Primary400 = Color(0xFF4D7D6A)  // Medium (Secondary)
val Primary500 = Color(0xFF1F3D32)  // Brand Primary
val Primary600 = Color(0xFF1A332A)  // Slightly darker
val Primary700 = Color(0xFF152822)  // Dark
val Primary800 = Color(0xFF0F1C18)  // Deep Forest
val Primary900 = Color(0xFF0A1210)  // Deepest

// ═══════════════════════════════════════════════════════════════════════════
// ACCENT PALETTE (Warm amber - Muted for minimal aesthetic)
// ═══════════════════════════════════════════════════════════════════════════

val Accent50 = Color(0xFFFCF9F5)    // Warmer, softer
val Accent100 = Color(0xFFF7EFE3)   // Very light
val Accent200 = Color(0xFFF0DCC5)   // Light
val Accent300 = Color(0xFFE5C49C)   // Medium light
val Accent400 = Color(0xFFCFA06A)   // Brand Accent (slightly muted)
val Accent500 = Color(0xFFB88A52)   // Medium
val Accent600 = Color(0xFF9A7144)   // Darker
val Accent700 = Color(0xFF7D5A37)   // Dark

// ═══════════════════════════════════════════════════════════════════════════
// NEUTRALS (Refined for calm, premium feel - NO pure black/white)
// ═══════════════════════════════════════════════════════════════════════════

// Light Mode Neutrals (warm undertone, calmer)
val Neutral50 = Color(0xFFFCFCFB)   // Off-white (warmest, softest)
val Neutral100 = Color(0xFFF8F8F6)  // Background light
val Neutral150 = Color(0xFFF5F4F1)  // Cream variant
val Neutral200 = Color(0xFFEDECE8)  // Surface
val Neutral300 = Color(0xFFE0DED9)  // Dividers (softer)
val Neutral400 = Color(0xFFC5C3BD)  // Borders
val Neutral500 = Color(0xFF9D9B94)  // Muted text
val Neutral600 = Color(0xFF757370)  // Secondary text (better contrast)
val Neutral700 = Color(0xFF545250)  // Primary text
val Neutral800 = Color(0xFF353432)  // Headlines
val Neutral900 = Color(0xFF1F1E1C)  // Darkest text (not pure black)

// Dark Mode Neutrals (forest undertone, refined)
val NeutralDark50 = Color(0xFF0C1410)   // Deepest background
val NeutralDark100 = Color(0xFF111A15)  // Background (from logo)
val NeutralDark200 = Color(0xFF182420)  // Elevated surface
val NeutralDark300 = Color(0xFF223330)  // Cards
val NeutralDark400 = Color(0xFF2E4440)  // Borders (softer)
val NeutralDark500 = Color(0xFF3D5650)  // Muted elements
val NeutralDark600 = Color(0xFF5A7570)  // Secondary text
val NeutralDark700 = Color(0xFF8AA5A0)  // Primary text
val NeutralDark800 = Color(0xFFBFCCC8)  // Bright text
val NeutralDark900 = Color(0xFFF0F2F1)  // Highest contrast (slightly muted)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC COLORS (Muted, calm versions - Non-aggressive)
// ═══════════════════════════════════════════════════════════════════════════

// Success - Soft sage green (calm, not harsh)
val SemanticSuccess = Color(0xFF5E9E75)
val SemanticSuccessLight = Color(0xFFEDF5F0)
val SemanticSuccessDark = Color(0xFF3D7A52)

// Warning - Uses accent amber family (warm, not alarming)
val SemanticWarning = Color(0xFFCFA06A)
val SemanticWarningLight = Color(0xFFFCF7F0)
val SemanticWarningDark = Color(0xFFB88A52)

// Error - Muted rose (noticeable but not aggressive)
val SemanticError = Color(0xFFCD7070)
val SemanticErrorLight = Color(0xFFFDF2F2)
val SemanticErrorDark = Color(0xFFAD5050)

// Info - Calm slate blue (quiet, informative)
val SemanticInfo = Color(0xFF6889B0)
val SemanticInfoLight = Color(0xFFF2F5F9)
val SemanticInfoDark = Color(0xFF4A6B92)

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENTS (Minimal - Flattened for calm aesthetic, subtle when needed)
// ═══════════════════════════════════════════════════════════════════════════

/** Main brand - Solid color for minimal look */
val GradientBrand = SolidColor(Primary500)

/** Subtle brand gradient for cards and backgrounds */
val GradientBrandSubtle = Brush.verticalGradient(
    colors = listOf(
        Primary500.copy(alpha = 0.08f),
        Primary500.copy(alpha = 0.02f)
    )
)

/** Subtle surface gradient for depth - Very subtle */
val GradientSurface = Brush.verticalGradient(
    colors = listOf(
        Neutral100,
        Neutral100.copy(alpha = 0.95f)
    )
)

val GradientSurfaceDark = Brush.verticalGradient(
    colors = listOf(
        NeutralDark200,
        NeutralDark200.copy(alpha = 0.95f)
    )
)

/** Accent - Solid for minimal look */
val GradientAccent = SolidColor(Accent400)

/** Glass highlight - Very subtle */
val GradientGlassHighlight = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.02f)
    )
)

/** Skeleton shimmer - Subtle animation */
val GradientShimmer = Brush.horizontalGradient(
    colors = listOf(
        Neutral200,
        Neutral100,
        Neutral200
    )
)

val GradientShimmerDark = Brush.horizontalGradient(
    colors = listOf(
        NeutralDark300,
        NeutralDark200,
        NeutralDark300
    )
)

/** Premium subtle gradient for hero sections */
val GradientPremiumLight = Brush.verticalGradient(
    colors = listOf(
        Primary50,
        Neutral100
    )
)

val GradientPremiumDark = Brush.verticalGradient(
    colors = listOf(
        NeutralDark100,
        NeutralDark200
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// COLOR SCHEME OBJECT (For CompositionLocal - Refined)
// ═══════════════════════════════════════════════════════════════════════════

data class HabitateColorScheme(
    // Brand
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    
    // Surfaces (Refined for minimal look)
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceElevated: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    
    // Glass (Minimal glassmorphism)
    val glassTint: Color,
    val glassBorder: Color,
    val glassHighlight: Brush,
    
    // Structural
    val divider: Color,
    val border: Color,
    val borderSubtle: Color,
    val shimmer: Brush,
    
    // Text hierarchy (Clear visual hierarchy)
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textInverse: Color,
    
    // Semantic
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    
    // Component specific (Premium feel)
    val fabBackground: Color,
    val fabContent: Color,
    val navBarBackground: Color,
    val navBarIndicator: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val chipBackground: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,
    
    // Interactive states
    val ripple: Color,
    val scrim: Color,
    val overlay: Color,
    
    val isDark: Boolean
)

val LightHabitateColors = HabitateColorScheme(
    // Brand
    primary = Primary500,
    onPrimary = BrandCream,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary700,
    
    secondary = Primary400,
    onSecondary = Neutral50,
    secondaryContainer = Primary50,
    onSecondaryContainer = Primary600,
    
    accent = Accent400,
    onAccent = Neutral900,
    accentContainer = Accent50,
    onAccentContainer = Accent700,
    
    // Surfaces (Refined - cleaner, calmer)
    background = Neutral100,
    onBackground = Neutral800,
    surface = Neutral50,
    onSurface = Neutral800,
    surfaceVariant = Neutral150,
    onSurfaceVariant = Neutral600,
    surfaceElevated = Neutral50,
    surfaceContainerLow = Neutral100,
    surfaceContainerHigh = Neutral200,
    
    // Glass (More subtle)
    glassTint = Primary500.copy(alpha = 0.04f),
    glassBorder = Primary500.copy(alpha = 0.08f),
    glassHighlight = GradientGlassHighlight,
    
    // Structural (Softer dividers)
    divider = Neutral300.copy(alpha = 0.6f),
    border = Neutral400,
    borderSubtle = Neutral300,
    shimmer = GradientShimmer,
    
    // Text (Clear hierarchy)
    textPrimary = Neutral800,
    textSecondary = Neutral600,
    textMuted = Neutral500,
    textDisabled = Neutral400,
    textInverse = Neutral50,
    
    // Semantic
    success = SemanticSuccess,
    onSuccess = Neutral50,
    successContainer = SemanticSuccessLight,
    warning = SemanticWarning,
    onWarning = Neutral900,
    warningContainer = SemanticWarningLight,
    error = SemanticError,
    onError = Neutral50,
    errorContainer = SemanticErrorLight,
    info = SemanticInfo,
    onInfo = Neutral50,
    infoContainer = SemanticInfoLight,
    
    // Components (Premium, minimal)
    fabBackground = Primary500,
    fabContent = BrandCream,
    navBarBackground = Neutral50.copy(alpha = 0.92f),
    navBarIndicator = Primary100,
    cardBackground = Neutral50,
    cardBorder = Neutral300.copy(alpha = 0.5f),
    chipBackground = Primary50,
    inputBackground = Neutral100,
    inputBorder = Neutral400,
    inputBorderFocused = Primary500,
    
    // Interactive
    ripple = Primary500.copy(alpha = 0.08f),
    scrim = Primary800.copy(alpha = 0.32f),
    overlay = Neutral900.copy(alpha = 0.4f),
    
    isDark = false
)

val DarkHabitateColors = HabitateColorScheme(
    // Brand (Elevated in dark mode)
    primary = Primary300,
    onPrimary = NeutralDark100,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary100,
    
    secondary = Primary400,
    onSecondary = NeutralDark100,
    secondaryContainer = NeutralDark300,
    onSecondaryContainer = NeutralDark800,
    
    accent = Accent300,
    onAccent = NeutralDark100,
    accentContainer = Accent700.copy(alpha = 0.25f),
    onAccentContainer = Accent100,
    
    // Surfaces (True dark, premium feel)
    background = NeutralDark100,
    onBackground = NeutralDark800,
    surface = NeutralDark200,
    onSurface = NeutralDark800,
    surfaceVariant = NeutralDark300,
    onSurfaceVariant = NeutralDark600,
    surfaceElevated = NeutralDark300,
    surfaceContainerLow = NeutralDark100,
    surfaceContainerHigh = NeutralDark300,
    
    // Glass (Subtle in dark mode)
    glassTint = Primary300.copy(alpha = 0.06f),
    glassBorder = Primary300.copy(alpha = 0.10f),
    glassHighlight = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.01f)
        )
    ),
    
    // Structural (Subtle)
    divider = NeutralDark400.copy(alpha = 0.6f),
    border = NeutralDark500,
    borderSubtle = NeutralDark400,
    shimmer = GradientShimmerDark,
    
    // Text (High contrast for readability)
    textPrimary = NeutralDark900,
    textSecondary = NeutralDark700,
    textMuted = NeutralDark600,
    textDisabled = NeutralDark500,
    textInverse = NeutralDark100,
    
    // Semantic
    success = SemanticSuccess,
    onSuccess = NeutralDark100,
    successContainer = SemanticSuccessDark.copy(alpha = 0.15f),
    warning = SemanticWarning,
    onWarning = NeutralDark100,
    warningContainer = SemanticWarningDark.copy(alpha = 0.15f),
    error = SemanticError,
    onError = NeutralDark100,
    errorContainer = SemanticErrorDark.copy(alpha = 0.15f),
    info = SemanticInfo,
    onInfo = NeutralDark100,
    infoContainer = SemanticInfoDark.copy(alpha = 0.15f),
    
    // Components (Subtle elevation)
    fabBackground = Primary400,
    fabContent = NeutralDark100,
    navBarBackground = NeutralDark200.copy(alpha = 0.95f),
    navBarIndicator = NeutralDark300,
    cardBackground = NeutralDark200,
    cardBorder = NeutralDark400.copy(alpha = 0.5f),
    chipBackground = NeutralDark300,
    inputBackground = NeutralDark300,
    inputBorder = NeutralDark500,
    inputBorderFocused = Primary400,
    
    // Interactive
    ripple = Primary300.copy(alpha = 0.10f),
    scrim = NeutralDark50.copy(alpha = 0.5f),
    overlay = NeutralDark50.copy(alpha = 0.6f),
    
    isDark = true
)

val LocalHabitateColors = staticCompositionLocalOf { LightHabitateColors }

// ═══════════════════════════════════════════════════════════════════════════
// LEGACY ALIASES (For backward compatibility)
// ═══════════════════════════════════════════════════════════════════════════

val Primary = Primary500
val PrimarySoft = Primary400
val PrimaryLight = BrandCream
val PrimaryDark = Primary800
val Cream = BrandCream
val ForestGreen = Primary500
val DeepForest = Primary800

val BackgroundLight = Neutral100
val SurfaceLight = Neutral50
val SurfaceSoftLight = Neutral150
val DividerLight = Neutral300
val BorderLight = Neutral400

val BackgroundDark = NeutralDark100
val SurfaceDark = NeutralDark200
val SurfaceSoftDark = NeutralDark300
val DividerDark = NeutralDark400
val BorderDark = NeutralDark500

val TextPrimaryLight = Neutral800
val TextSecondaryLight = Neutral600
val TextMutedLight = Neutral500
val TextDisabledLight = Neutral400

val TextPrimaryDark = NeutralDark900
val TextSecondaryDark = NeutralDark700
val TextMutedDark = NeutralDark600
val TextDisabledDark = NeutralDark500

val Success = SemanticSuccess
val Warning = SemanticWarning
val Error = SemanticError
val Info = SemanticInfo

val PrimaryGradient = GradientBrand
val BrandGradient = GradientBrand
val SunriseGradient = GradientAccent
val FocusGradient = Brush.horizontalGradient(
    colors = listOf(Primary600, Primary400, Primary300)
)
