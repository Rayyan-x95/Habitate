package com.ninety5.habitate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - TYPOGRAPHY                   ║
 * ║                         Version 3.0 - 2026 Spatial UI                    ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Quietly powerful, effortless to read                                  ║
 * ║  • Generous line heights for calm reading (1.5x)                         ║
 * ║  • Avoid excessive bold - use weight meaningfully                        ║
 * ║  • Clear hierarchy without visual noise                                  ║
 * ║                                                                          ║
 * ║  Hierarchy: Display → Headline → Title → Body → Label                    ║
 * ║                                                                          ║
 * ║  Inspiration: Apple Health, Linear, Notion                               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// FONT FAMILY
// ═══════════════════════════════════════════════════════════════════════════

// Using system default for now (Roboto on Android)
// To use Google Sans Flex: add the font files to res/font/ and uncomment below.
// val GoogleSansFlex = FontFamily(
//     Font(R.font.google_sans_flex_regular, FontWeight.Normal),
//     Font(R.font.google_sans_flex_medium, FontWeight.Medium),
//     Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
// )

val AppFontFamily = FontFamily.Default

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC TYPOGRAPHY ALIASES (Used by design-system components)
// Maps semantic names to Material 3 scale
// ═══════════════════════════════════════════════════════════════════════════

/** Screen / page titles (headlineMedium) */
val ScreenTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/** Section headers inside screens (titleLarge) */
val SectionTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp
)

/** Card / list item title (titleMedium) */
val CardTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

/** Primary body text (bodyMedium) */
val BodyText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.25.sp
)

/** Supporting / secondary description text (bodySmall) */
val SupportingText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp
)

/** Metadata, timestamps, overlines (labelSmall) */
val MetaText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

/** Caption / fine-print text (labelSmall) */
val CaptionText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

/** Button label text (labelLarge) */
val ButtonText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

// ═══════════════════════════════════════════════════════════════════════════
// TYPOGRAPHY SCALE (Refined for calm, minimal aesthetic)
// ═══════════════════════════════════════════════════════════════════════════

val Typography = Typography(
    // ─────────────────────────────────────────────────────────────────────────
    // DISPLAY - Hero text, splash screens, onboarding
    // Use sparingly - these are for impact moments
    // ─────────────────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // HEADLINE - Screen titles, section headers
    // Primary navigation and section markers
    // ─────────────────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // TITLE - Card titles, list headers, dialog titles
    // ─────────────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // BODY - Main content, descriptions, long-form text
    // ─────────────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // LABEL - Buttons, tags, overlines, tiny metadata
    // ─────────────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
