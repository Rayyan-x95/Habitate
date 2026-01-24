package com.ninety5.habitate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - TYPOGRAPHY                   ║
 * ║                         Version 2.0 - Minimal Redesign                   ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Quietly powerful, effortless to read                                  ║
 * ║  • Generous line heights for calm reading                                ║
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
// TODO: Add Google Sans Flex when available in res/font
// val GoogleSansFlex = FontFamily(
//     Font(R.font.google_sans_flex_regular, FontWeight.Normal),
//     Font(R.font.google_sans_flex_medium, FontWeight.Medium),
//     Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
// )

val AppFontFamily = FontFamily.Default

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
        fontWeight = FontWeight.Medium,  // Reduced from SemiBold
        fontSize = 36.sp,                 // Slightly smaller
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,   // Lighter weight
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // HEADLINE - Screen titles, section headers
    // Primary navigation and section markers
    // ─────────────────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,   // Reduced from SemiBold
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,   // Lighter for subtlety
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // TITLE - Card titles, list headers, dialog titles
    // ─────────────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // BODY - Main content, descriptions, paragraphs
    // Generous line heights for comfortable reading
    // ─────────────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,       // 1.625 ratio - very comfortable
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,       // 1.57 ratio
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,       // 1.5 ratio
        letterSpacing = 0.2.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // LABEL - Buttons, chips, navigation, metadata
    // Slightly more tracking for clarity at small sizes
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
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC TYPOGRAPHY ALIASES
// Use these for consistent styling across the app
// ═══════════════════════════════════════════════════════════════════════════

/** Screen titles - Top app bar, main headers */
val ScreenTitle = Typography.headlineLarge

/** Section headers - Within-screen sections */
val SectionTitle = Typography.headlineMedium

/** Card titles - Card headers, list item titles */
val CardTitle = Typography.titleMedium

/** Primary body text - Main content */
val BodyText = Typography.bodyLarge

/** Secondary/supporting text - Descriptions, help text */
val SupportingText = Typography.bodyMedium

/** Metadata - Timestamps, counts, hints */
val MetaText = Typography.labelMedium

/** Captions - Image captions, small annotations */
val CaptionText = Typography.labelSmall

/** Button text - All button labels */
val ButtonText = Typography.labelLarge

/** Badge/chip text - Tiny labels in badges */
val BadgeText = Typography.labelSmall

/** Emphasis text - When you need slight emphasis in body */
val EmphasisText = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.15.sp
)

/** Mono text - For code, numbers */
val MonoText = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)
