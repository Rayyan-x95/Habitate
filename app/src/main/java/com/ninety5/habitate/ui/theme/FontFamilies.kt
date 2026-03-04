package com.ninety5.habitate.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.ninety5.habitate.R

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE FONT FAMILIES                                ║
 * ║                                                                          ║
 * ║  Four font families powering the design system:                          ║
 * ║                                                                          ║
 * ║  1. Google Sans Flex  — Primary UI font (Display/Headlines/Body)         ║
 * ║     Variable: weights 100–1000, optical size 8–144                       ║
 * ║     Source: Google Fonts (downloadable)                                   ║
 * ║                                                                          ║
 * ║  2. Inter — Body text alternative / fallback                             ║
 * ║     Variable: weights 100–900, optical size 14–32                        ║
 * ║     Source: Google Fonts (downloadable)                                   ║
 * ║                                                                          ║
 * ║  3. Space Grotesk — Accent headings / monospace-adjacent                 ║
 * ║     Weights: 300–700                                                     ║
 * ║     Source: Google Fonts (downloadable)                                   ║
 * ║                                                                          ║
 * ║  4. Poppins — Downloadable font (branding, special usage)              ║
 * ║     Weights: Thin–Black (100–900)                                      ║
 * ║     Source: Google Fonts (downloadable, OFL-1.1 license)               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// GOOGLE FONTS PROVIDER
// ═══════════════════════════════════════════════════════════════════════════

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ═══════════════════════════════════════════════════════════════════════════
// 1. GOOGLE SANS FLEX (Downloadable — Primary App Font)
//    Variable font: weight 100–1000, optical size 8–144
// ═══════════════════════════════════════════════════════════════════════════

private val googleSansFlexFont = GoogleFont("Google Sans Flex")

val GoogleSansFlexFamily = FontFamily(
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Thin),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraLight),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Black),
    // Italic variants
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(googleFont = googleSansFlexFont, fontProvider = googleFontProvider, weight = FontWeight.Bold, style = FontStyle.Italic),
)

// ═══════════════════════════════════════════════════════════════════════════
// 2. INTER (Downloadable — Body Text / Fallback)
//    Variable font: weight 100–900, optical size 14–32
// ═══════════════════════════════════════════════════════════════════════════

private val interFont = GoogleFont("Inter")

val InterFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Thin),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraLight),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Black),
    // Italic variants
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold, style = FontStyle.Italic),
)

// ═══════════════════════════════════════════════════════════════════════════
// 3. SPACE GROTESK (Downloadable — Accent Headings)
//    Weights: 300–700
// ═══════════════════════════════════════════════════════════════════════════

private val spaceGroteskFont = GoogleFont("Space Grotesk")

val SpaceGroteskFamily = FontFamily(
    Font(googleFont = spaceGroteskFont, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = spaceGroteskFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceGroteskFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = spaceGroteskFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGroteskFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

// ═══════════════════════════════════════════════════════════════════════════
// 4. POPPINS (Downloadable — Branding / Special Usage)
//    Weights: Thin (100) – Black (900)
//    License: OFL-1.1 (SIL Open Font License)
// ═══════════════════════════════════════════════════════════════════════════

private val poppinsFont = GoogleFont("Poppins")

val PoppinsFamily = FontFamily(
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Thin),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraLight),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Black),
    // Italic variants
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(googleFont = poppinsFont, fontProvider = googleFontProvider, weight = FontWeight.Bold, style = FontStyle.Italic),
)

/**
 * Legacy alias for GoogleSansFamily → now backed by Poppins.
 * Kept for source compatibility at existing call sites.
 */
@Deprecated("Use PoppinsFamily directly", ReplaceWith("PoppinsFamily"))
val GoogleSansFamily = PoppinsFamily
