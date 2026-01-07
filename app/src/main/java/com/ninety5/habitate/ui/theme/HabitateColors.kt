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
 * ║                                                                          ║
 * ║  All colors derived from app logo:                                       ║
 * ║  • Primary: Dark Forest Green (#1F3D32)                                 ║
 * ║  • Secondary: Sage Green (desaturated primary)                          ║
 * ║  • Accent: Warm Amber (complementary warmth)                            ║
 * ║  • Neutrals: Desaturated greens (no pure black/white)                   ║
 * ║                                                                          ║
 * ║  Philosophy: Calm • Trustworthy • Premium • Human                       ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// BRAND CORE (From Logo)
// ═══════════════════════════════════════════════════════════════════════════

/** Primary brand color - Forest Green from logo background */
val BrandPrimary = Color(0xFF1F3D32)

/** Secondary brand - Lighter sage derived from primary */
val BrandSecondary = Color(0xFF3D6B5C)

/** Accent - Warm amber for highlights & interactions */
val BrandAccent = Color(0xFFD4A574)

/** Cream - Logo color for contrast elements */
val BrandCream = Color(0xFFF2EFEA)

/** Deep Forest - Darkest brand shade */
val BrandDeepForest = Color(0xFF0F1C18)

// ═══════════════════════════════════════════════════════════════════════════
// PRIMARY PALETTE (5 shades from logo green)
// ═══════════════════════════════════════════════════════════════════════════

val Primary50 = Color(0xFFF0F5F3)   // Lightest tint
val Primary100 = Color(0xFFD4E4DE)  // Very light
val Primary200 = Color(0xFFA8C9BC)  // Light
val Primary300 = Color(0xFF6B9D89)  // Medium light
val Primary400 = Color(0xFF3D6B5C)  // Medium (Secondary)
val Primary500 = Color(0xFF1F3D32)  // Brand Primary
val Primary600 = Color(0xFF1A332A)  // Slightly darker
val Primary700 = Color(0xFF152822)  // Dark
val Primary800 = Color(0xFF0F1C18)  // Deep Forest
val Primary900 = Color(0xFF0A1210)  // Deepest

// ═══════════════════════════════════════════════════════════════════════════
// ACCENT PALETTE (Warm amber - complementary to forest green)
// ═══════════════════════════════════════════════════════════════════════════

val Accent50 = Color(0xFFFDF8F3)
val Accent100 = Color(0xFFF8EBDB)
val Accent200 = Color(0xFFF0D4B3)
val Accent300 = Color(0xFFE8BD8B)
val Accent400 = Color(0xFFD4A574)   // Brand Accent
val Accent500 = Color(0xFFC08C5B)
val Accent600 = Color(0xFFA67347)
val Accent700 = Color(0xFF8A5D3A)

// ═══════════════════════════════════════════════════════════════════════════
// NEUTRALS (Desaturated forest green - NO pure black/white)
// ═══════════════════════════════════════════════════════════════════════════

// Light Mode Neutrals (warm undertone from logo)
val Neutral50 = Color(0xFFFAFBFA)   // Off-white (warmest)
val Neutral100 = Color(0xFFF5F7F6)  // Background light
val Neutral150 = Color(0xFFF2EFEA)  // Cream (from logo)
val Neutral200 = Color(0xFFE8EDEA)  // Surface
val Neutral300 = Color(0xFFD4DDD8)  // Dividers
val Neutral400 = Color(0xFFB0BFB7)  // Borders
val Neutral500 = Color(0xFF8A9C92)  // Muted text
val Neutral600 = Color(0xFF667A70)  // Secondary text
val Neutral700 = Color(0xFF4A5C53)  // Primary text
val Neutral800 = Color(0xFF2E3D36)  // Headlines
val Neutral900 = Color(0xFF1A2720)  // Darkest text (not pure black)

// Dark Mode Neutrals (forest undertone)
val NeutralDark50 = Color(0xFF0A1210)   // Deepest background
val NeutralDark100 = Color(0xFF0F1C18)  // Background (from logo)
val NeutralDark200 = Color(0xFF152822)  // Elevated surface
val NeutralDark300 = Color(0xFF1F3830)  // Cards
val NeutralDark400 = Color(0xFF2A4A3F)  // Borders
val NeutralDark500 = Color(0xFF3D5C50)  // Muted elements
val NeutralDark600 = Color(0xFF5A7A6C)  // Secondary text
val NeutralDark700 = Color(0xFF8FAEA0)  // Primary text
val NeutralDark800 = Color(0xFFB8C4BE)  // Bright text
val NeutralDark900 = Color(0xFFF2EFEA)  // Cream (highest contrast)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC COLORS (Muted, calm versions)
// ═══════════════════════════════════════════════════════════════════════════

val SemanticSuccess = Color(0xFF5D9B6D)      // Muted green (not harsh)
val SemanticSuccessLight = Color(0xFFE8F5EB)
val SemanticSuccessDark = Color(0xFF3D7A4D)

val SemanticWarning = Color(0xFFD4A574)      // Uses accent amber
val SemanticWarningLight = Color(0xFFFDF4E8)
val SemanticWarningDark = Color(0xFFC08C5B)

val SemanticError = Color(0xFFCF6B6B)        // Muted rose (not aggressive red)
val SemanticErrorLight = Color(0xFFFDF0F0)
val SemanticErrorDark = Color(0xFFB54B4B)

val SemanticInfo = Color(0xFF6B8ECF)         // Calm blue
val SemanticInfoLight = Color(0xFFF0F4FD)
val SemanticInfoDark = Color(0xFF4B6BB5)

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENTS (Logo-derived, subtle)
// ═══════════════════════════════════════════════════════════════════════════

/** Main brand gradient - flattened to solid for minimal look */
val GradientBrand = SolidColor(Primary500)

/** Subtle surface gradient for depth - flattened */
val GradientSurface = SolidColor(Neutral100)

val GradientSurfaceDark = SolidColor(NeutralDark200)

/** Accent gradient for highlights - flattened */
val GradientAccent = SolidColor(Accent500)

/** Glass highlight gradient */
val GradientGlassHighlight = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f)
    )
)

/** Skeleton shimmer gradient */
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

// ═══════════════════════════════════════════════════════════════════════════
// COLOR SCHEME OBJECT (For CompositionLocal)
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
    
    // Surfaces
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceElevated: Color,
    
    // Glass
    val glassTint: Color,
    val glassBorder: Color,
    val glassHighlight: Brush,
    
    // Structural
    val divider: Color,
    val border: Color,
    val shimmer: Brush,
    
    // Text hierarchy
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    
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
    
    // Component specific
    val fabBackground: Color,
    val fabContent: Color,
    val navBarBackground: Color,
    val cardBackground: Color,
    val chipBackground: Color,
    val inputBackground: Color,
    
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
    secondaryContainer = Primary100,
    onSecondaryContainer = Primary600,
    
    accent = Accent400,
    onAccent = Neutral900,
    accentContainer = Accent100,
    onAccentContainer = Accent700,
    
    // Surfaces
    background = Neutral100,
    onBackground = Neutral800,
    surface = Neutral50,
    onSurface = Neutral800,
    surfaceVariant = Neutral150,
    onSurfaceVariant = Neutral600,
    surfaceElevated = Color.White.copy(alpha = 0.8f),
    
    // Glass
    glassTint = Primary500.copy(alpha = 0.08f),
    glassBorder = Primary500.copy(alpha = 0.12f),
    glassHighlight = GradientGlassHighlight,
    
    // Structural
    divider = Neutral300,
    border = Neutral400,
    shimmer = GradientShimmer,
    
    // Text
    textPrimary = Neutral800,
    textSecondary = Neutral600,
    textMuted = Neutral500,
    textDisabled = Neutral400,
    
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
    
    // Components
    fabBackground = Primary500,
    fabContent = BrandCream,
    navBarBackground = Neutral50.copy(alpha = 0.85f),
    cardBackground = Neutral50,
    chipBackground = Primary100,
    inputBackground = Neutral200,
    
    isDark = false
)

val DarkHabitateColors = HabitateColorScheme(
    // Brand
    primary = Primary400,
    onPrimary = NeutralDark100,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary200,
    
    secondary = Primary300,
    onSecondary = NeutralDark100,
    secondaryContainer = NeutralDark300,
    onSecondaryContainer = NeutralDark800,
    
    accent = Accent400,
    onAccent = NeutralDark100,
    accentContainer = Accent700.copy(alpha = 0.3f),
    onAccentContainer = Accent200,
    
    // Surfaces
    background = NeutralDark100,
    onBackground = NeutralDark800,
    surface = NeutralDark200,
    onSurface = NeutralDark800,
    surfaceVariant = NeutralDark300,
    onSurfaceVariant = NeutralDark600,
    surfaceElevated = NeutralDark300.copy(alpha = 0.9f),
    
    // Glass
    glassTint = Primary400.copy(alpha = 0.12f),
    glassBorder = Primary400.copy(alpha = 0.18f),
    glassHighlight = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    ),
    
    // Structural
    divider = NeutralDark400,
    border = NeutralDark500,
    shimmer = GradientShimmerDark,
    
    // Text
    textPrimary = NeutralDark900,
    textSecondary = NeutralDark700,
    textMuted = NeutralDark600,
    textDisabled = NeutralDark500,
    
    // Semantic
    success = SemanticSuccess,
    onSuccess = NeutralDark100,
    successContainer = SemanticSuccessDark.copy(alpha = 0.2f),
    warning = SemanticWarning,
    onWarning = NeutralDark100,
    warningContainer = SemanticWarningDark.copy(alpha = 0.2f),
    error = SemanticError,
    onError = NeutralDark100,
    errorContainer = SemanticErrorDark.copy(alpha = 0.2f),
    info = SemanticInfo,
    onInfo = NeutralDark100,
    infoContainer = SemanticInfoDark.copy(alpha = 0.2f),
    
    // Components
    fabBackground = Primary400,
    fabContent = NeutralDark100,
    navBarBackground = NeutralDark200.copy(alpha = 0.92f),
    cardBackground = NeutralDark200,
    chipBackground = NeutralDark300,
    inputBackground = NeutralDark300,
    
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
