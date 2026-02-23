package com.ninety5.habitate.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║          DEPRECATED — HABITATE REFERENCE DESIGN TOKENS                    ║
 * ║                                                                          ║
 * ║  This file is a parallel color/token system extracted from screenshots.  ║
 * ║  It duplicates HabitateColors.kt and DesignTokens.kt and violates the   ║
 * ║  brand guidelines (pure black text, alien blue accent).                  ║
 * ║                                                                          ║
 * ║  Migration:                                                              ║
 * ║    ReferenceColors.primary     → HabitateTheme.colors.primary           ║
 * ║    ReferenceColors.onPrimary   → HabitateTheme.colors.onPrimary         ║
 * ║    ReferenceColors.background  → HabitateTheme.colors.background        ║
 * ║    ReferenceColors.accent      → HabitateTheme.colors.accent            ║
 * ║    ReferenceColors.textPrimary → HabitateTheme.colors.textPrimary       ║
 * ║    ReferenceColors.border      → HabitateTheme.colors.border            ║
 * ║                                                                          ║
 * ║  Screens to migrate: LoginScreen, ConversationsListScreen,              ║
 * ║                       FeedbackSystem                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// REFERENCE COLORS (EXTRACTED FROM SCREENSHOTS)
// ═══════════════════════════════════════════════════════════════════════════

/** Dark forest green from splash/onboarding screens */
val RefDarkGreen = Color(0xFF1F3D32)

/** Light gray background from login screen */
val RefLightGrayBg = Color(0xFFF5F5F5)

/** Login button blue */
val RefLoginButtonBlue = Color(0xFF2563EB)

/** White/cream from logo and text */
val RefWhite = Color(0xFFF2EFEA)

/** Dark text for login screen - improved contrast */
val RefDarkText = Color(0xFF000000)

/** Gray text for placeholders and subtitles - improved contrast for WCAG AA */
val RefGrayText = Color(0xFF424242)

/** Light gray for dividers and borders */
val RefLightGray = Color(0xFFE0E0E0)

/** Social button background */
val RefSocialButtonBg = Color(0xFFFFFFFF)

/** Google red */
val RefGoogleRed = Color(0xFFDB4437)

// ═══════════════════════════════════════════════════════════════════════════
// SPACING TOKENS (MEASURED FROM REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Base spacing unit (4dp) */
val RefSpacingXS = 4

/** Small spacing (8dp) */
val RefSpacingSM = 8

/** Medium spacing (16dp) */
val RefSpacingMD = 16

/** Large spacing (24dp) */
val RefSpacingLG = 24

/** Extra large spacing (32dp) */
val RefSpacingXL = 32

/** Huge spacing (48dp) */
val RefSpacingXXL = 48

/** Card padding (24dp) */
val RefCardPadding = 24

/** Screen padding (24dp) */
val RefScreenPadding = 24

// ═══════════════════════════════════════════════════════════════════════════
// TYPOGRAPHY TOKENS (MATCHED TO REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Large title text (32sp) - for onboarding */
val RefTextSizeXXL = 32

/** Headline text (28sp) - for login title */
val RefTextSizeXL = 28

/** Body text (16sp) - for buttons */
val RefTextSizeLG = 16

/** Regular text (14sp) - for descriptions */
val RefTextSizeMD = 14

/** Small text (12sp) - for links and small labels */
val RefTextSizeSM = 12

/** Tiny text (10sp) - for very small elements */
val RefTextSizeXS = 10

// ═══════════════════════════════════════════════════════════════════════════
// LINE HEIGHT TOKENS (IMPROVED READABILITY)
// ═══════════════════════════════════════════════════════════════════════════

/** Line height for headings (1.3 ratio) */
val RefLineHeightHeading = 1.3f

/** Line height for body text (1.5 ratio) */
val RefLineHeightBody = 1.5f

/** Line height for dense text (1.2 ratio) */
val RefLineHeightDense = 1.2f

// ═══════════════════════════════════════════════════════════════════════════
// ICON SIZES (SYSTEMATIC APPROACH)
// ═══════════════════════════════════════════════════════════════════════════

/** Extra small icons (16dp) - for dense UI elements */
val RefIconSizeXS = 16

/** Small icons (20dp) - for buttons and inline actions */
val RefIconSizeSM = 20

/** Medium icons (24dp) - for standard actions */
val RefIconSizeMD = 24

/** Large icons (32dp) - for navigation and emphasis */
val RefIconSizeLG = 32

/** Extra large icons (40dp) - for FABs and hero actions */
val RefIconSizeXL = 40

/** Hero icons (48dp) - for empty states and major actions */
val RefIconSizeXXL = 48

// ═══════════════════════════════════════════════════════════════════════════
// COMPONENT DIMENSIONS (MEASURED FROM REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Button height (48dp) */
val RefButtonHeight = 48

/** Social button size (56dp) */
val RefSocialButtonSize = 56

/** Logo size (64dp) */
val RefLogoSize = 64

/** Circular navigation button (64dp) */
val RefCircularButtonSize = 64

/** Input field height (48dp) */
val RefInputHeight = 48

/** Avatar size (40dp) */
val RefAvatarSize = 40

/** Large avatar size (80dp) */
val RefAvatarSizeLG = 80

// ═══════════════════════════════════════════════════════════════════════════
// CORNER RADII (MATCHED TO REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Small corner radius (8dp) - for inputs and buttons */
val RefRadiusSM = 8

/** Medium corner radius (12dp) - for social buttons */
val RefRadiusMD = 12

/** Large corner radius (16dp) - for cards */
val RefRadiusLG = 16

/** Full circle - for avatars and circular buttons */
val RefRadiusFull = 1000 // Large enough to be circular

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENTS (FROM REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Dark green gradient for splash/onboarding backgrounds */
val RefDarkGreenGradient = Brush.verticalGradient(
    colors = listOf(
        RefDarkGreen,
        Color(0xFF0F1C18) // Slightly darker version
    )
)

/** Light gray gradient for login background */
val RefLightGrayGradient = Brush.verticalGradient(
    colors = listOf(
        RefLightGrayBg,
        Color(0xFFEEEEEE) // Slightly darker version
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// SHADOWS & ELEVATION (MATCHED TO REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Card shadow elevation (2dp) */
val RefCardElevation = 2

/** Button shadow elevation (1dp) */
val RefButtonElevation = 1

/** Social button shadow elevation (2dp) */
val RefSocialButtonElevation = 2

// ═══════════════════════════════════════════════════════════════════════════
// OPACITY VALUES (FROM REFERENCE)
// ═══════════════════════════════════════════════════════════════════════════

/** Disabled opacity */
val RefOpacityDisabled = 0.5f

/** Divider opacity */
val RefOpacityDivider = 0.3f

/** Overlay opacity */
val RefOpacityOverlay = 0.1f

// ═══════════════════════════════════════════════════════════════════════════
// REFERENCE COLOR SCHEME OBJECT — DEPRECATED
// ═══════════════════════════════════════════════════════════════════════════

@Deprecated("Use HabitateTheme.colors instead — this parallel color system will be removed")
data class ReferenceColorScheme(
    // Primary colors from reference
    val primary: Color = RefDarkGreen,
    val primaryVariant: Color = Color(0xFF0F1C18),
    val onPrimary: Color = RefWhite,
    
    // Background colors
    val background: Color = RefDarkGreen,
    val backgroundLight: Color = RefLightGrayBg,
    val onBackground: Color = RefWhite,
    val onBackgroundLight: Color = RefDarkText,
    
    // Surface colors
    val surface: Color = RefWhite,
    val onSurface: Color = RefDarkText,
    val surfaceVariant: Color = RefLightGray,
    
    // Accent colors
    val accent: Color = RefLoginButtonBlue,
    val onAccent: Color = RefWhite,
    
    // Text colors
    val textPrimary: Color = RefDarkText,
    val textSecondary: Color = RefGrayText,
    val textOnDark: Color = RefWhite,
    
    // Border and divider colors
    val border: Color = RefLightGray,
    val divider: Color = RefLightGray.copy(alpha = RefOpacityDivider),
    
    // Social colors
    val socialButtonBg: Color = RefSocialButtonBg,
    val googleRed: Color = RefGoogleRed,
    
    // Gradients
    val primaryGradient: Brush = RefDarkGreenGradient,
    val lightGradient: Brush = RefLightGrayGradient
)

/** Reference color scheme instance — DEPRECATED: Use HabitateTheme.colors */
@Deprecated("Use HabitateTheme.colors instead — this parallel color system will be removed")
@Suppress("DEPRECATION")
val ReferenceColors = ReferenceColorScheme()
