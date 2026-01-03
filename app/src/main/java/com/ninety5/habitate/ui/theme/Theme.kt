package com.ninety5.habitate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - THEME                        ║
 * ║                                                                          ║
 * ║  Provides:                                                                ║
 * ║  • Material 3 color scheme                                               ║
 * ║  • Custom Habitate color tokens                                          ║
 * ║  • Design tokens (spacing, sizing, etc.)                                 ║
 * ║  • Typography & shapes                                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// MATERIAL 3 COLOR SCHEMES
// ═══════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    // Primary
    primary = Primary500,
    onPrimary = BrandCream,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary700,
    
    // Secondary
    secondary = Primary400,
    onSecondary = Neutral50,
    secondaryContainer = Primary100,
    onSecondaryContainer = Primary600,
    
    // Tertiary (Accent)
    tertiary = Accent400,
    onTertiary = Neutral900,
    tertiaryContainer = Accent100,
    onTertiaryContainer = Accent700,
    
    // Background & Surface
    background = Neutral100,
    onBackground = Neutral800,
    surface = Neutral50,
    onSurface = Neutral800,
    surfaceVariant = Neutral150,
    onSurfaceVariant = Neutral600,
    
    // Error
    error = SemanticError,
    onError = Neutral50,
    errorContainer = SemanticErrorLight,
    onErrorContainer = SemanticErrorDark,
    
    // Outline & Dividers
    outline = Neutral400,
    outlineVariant = Neutral300,
    
    // Inverse
    inverseSurface = NeutralDark200,
    inverseOnSurface = NeutralDark800,
    inversePrimary = Primary300,
    
    // Scrim
    scrim = Primary800.copy(alpha = 0.32f),
    
    // Surface tones
    surfaceTint = Primary500
)

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = Primary300,
    onPrimary = Primary800,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary100,
    
    // Secondary
    secondary = Primary400,
    onSecondary = NeutralDark100,
    secondaryContainer = NeutralDark300,
    onSecondaryContainer = NeutralDark800,
    
    // Tertiary (Accent)
    tertiary = Accent300,
    onTertiary = Accent700,
    tertiaryContainer = Accent700.copy(alpha = 0.3f),
    onTertiaryContainer = Accent100,
    
    // Background & Surface
    background = NeutralDark100,
    onBackground = NeutralDark800,
    surface = NeutralDark200,
    onSurface = NeutralDark800,
    surfaceVariant = NeutralDark300,
    onSurfaceVariant = NeutralDark600,
    
    // Error
    error = SemanticError,
    onError = NeutralDark100,
    errorContainer = SemanticErrorDark.copy(alpha = 0.2f),
    onErrorContainer = SemanticErrorLight,
    
    // Outline & Dividers
    outline = NeutralDark500,
    outlineVariant = NeutralDark400,
    
    // Inverse
    inverseSurface = Neutral100,
    inverseOnSurface = Neutral800,
    inversePrimary = Primary500,
    
    // Scrim
    scrim = NeutralDark50.copy(alpha = 0.5f),
    
    // Surface tones
    surfaceTint = Primary400
)

// ═══════════════════════════════════════════════════════════════════════════
// HABITATE THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to use our brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Select Material 3 color scheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Select Habitate custom colors
    val habitateColors = if (darkTheme) DarkHabitateColors else LightHabitateColors

    // Configure status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use background color for status bar (more subtle)
            window.statusBarColor = colorScheme.background.toArgb()
            // Navigation bar
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Light/dark icons
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Provide all design tokens
    CompositionLocalProvider(
        LocalHabitateColors provides habitateColors,
        LocalHabitateTokens provides DefaultHabitateTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// THEME ACCESS HELPERS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Access Habitate-specific colors: HabitateTheme.colors.primary
 */
object HabitateTheme {
    val colors: HabitateColorScheme
        @Composable
        get() = LocalHabitateColors.current
    
    val tokens: HabitateTokens
        @Composable
        get() = LocalHabitateTokens.current
}
