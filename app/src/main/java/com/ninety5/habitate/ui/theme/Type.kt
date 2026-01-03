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
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Editorial, not mechanical                                             ║
 * ║  • Large headers, relaxed body text                                      ║
 * ║  • Font weights only where meaningful                                    ║
 * ║  • Avoid dense paragraphs                                                ║
 * ║                                                                          ║
 * ║  Hierarchy: Display → Section → Body → Meta                              ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// Using system default (will fallback to Roboto on Android)
// For production, consider adding Google Sans Flex or similar
val AppFontFamily = FontFamily.Default

// ═══════════════════════════════════════════════════════════════════════════
// TYPOGRAPHY SCALE
// ═══════════════════════════════════════════════════════════════════════════

val Typography = Typography(
    // ─────────────────────────────────────────────────────────────────────────
    // DISPLAY - Hero text, splash screens, large titles
    // ─────────────────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // HEADLINE - Screen titles, section headers
    // ─────────────────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
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
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // TITLE - Card titles, list headers, dialog titles
    // ─────────────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // BODY - Main content, descriptions, paragraphs
    // ─────────────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,  // Relaxed line height for readability
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),
    
    // ─────────────────────────────────────────────────────────────────────────
    // LABEL - Buttons, chips, navigation, metadata
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
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC TYPOGRAPHY ALIASES
// ═══════════════════════════════════════════════════════════════════════════

/** For screen titles */
val ScreenTitle = Typography.headlineLarge

/** For section headers */
val SectionTitle = Typography.headlineMedium

/** For card titles */
val CardTitle = Typography.titleMedium

/** For primary body text */
val BodyText = Typography.bodyLarge

/** For secondary/supporting text */
val SupportingText = Typography.bodyMedium

/** For metadata, timestamps, hints */
val MetaText = Typography.labelMedium

/** For buttons and actions */
val ButtonText = Typography.labelLarge

/** For tiny labels, badges */
val CaptionText = Typography.labelSmall
