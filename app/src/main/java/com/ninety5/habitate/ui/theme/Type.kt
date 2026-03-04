package com.ninety5.habitate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - TYPOGRAPHY                   ║
 * ║                         Version 4.0 - 2026 Spatial UI                    ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Quietly powerful, effortless to read                                  ║
 * ║  • Generous line heights for calm reading (1.5x)                         ║
 * ║  • Avoid excessive bold - use weight meaningfully                        ║
 * ║  • Clear hierarchy without visual noise                                  ║
 * ║                                                                          ║
 * ║  Font Families (see FontFamilies.kt):                                    ║
 * ║  • Google Sans Flex — Primary UI (Display, Headline, Title, Label)       ║
 * ║  • Inter — Body text (optimized for readability at small sizes)          ║
 * ║  • Space Grotesk — Accent headings (distinctive geometric sans)          ║
 * ║  • Google Sans — Branding / special usage (bundled)                      ║
 * ║                                                                          ║
 * ║  Hierarchy: Display → Headline → Title → Body → Label                    ║
 * ║                                                                          ║
 * ║  Inspiration: Apple Health, Linear, Notion                               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// FONT FAMILY ALIASES
// Google Sans Flex = primary UI font (Display, Headlines, Titles, Labels)
// Inter = body/reading font (Body text, Supporting text)
// Space Grotesk & Google Sans available via FontFamilies.kt
// ═══════════════════════════════════════════════════════════════════════════

/** Primary UI font — used for Display, Headline, Title, Label */
val AppFontFamily: FontFamily = GoogleSansFlexFamily

/** Reading / body font — optimized for long-form text at small sizes */
val BodyFontFamily: FontFamily = InterFamily

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC TYPOGRAPHY ALIASES (Used by design-system components)
// Maps semantic names to Material 3 scale
// ═══════════════════════════════════════════════════════════════════════════

/** Screen / page titles (headlineMedium) — Google Sans Flex */
val ScreenTitle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/** Section headers inside screens (titleLarge) — Google Sans Flex */
val SectionTitle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp
)

/** Card / list item title (titleMedium) — Google Sans Flex */
val CardTitle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

/** Primary body text (bodyMedium) — Inter */
val BodyText = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.25.sp
)

/** Supporting / secondary description text (bodySmall) — Inter */
val SupportingText = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp
)

/** Metadata, timestamps, overlines (labelSmall) — Google Sans Flex */
val MetaText = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

/** Caption / fine-print text (labelSmall) — Inter */
val CaptionText = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

/** Button label text (labelLarge) — Google Sans Flex */
val ButtonText = TextStyle(
    fontFamily = AppFontFamily,
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
    // DISPLAY - Hero text, splash screens, onboarding (Google Sans Flex)
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
    // HEADLINE - Screen titles, section headers (Google Sans Flex)
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
    // TITLE - Card titles, list headers, dialog titles (Google Sans Flex)
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
    // BODY - Main content, descriptions, long-form text (Inter)
    // Inter is optimized for readability at small sizes with optical sizing
    // ─────────────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // LABEL - Buttons, tags, overlines, tiny metadata (Google Sans Flex)
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
