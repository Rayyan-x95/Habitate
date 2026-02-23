package com.ninety5.habitate.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════
// LEGACY COLOR ALIASES — Scheduled for removal
// ═══════════════════════════════════════════════════════════════════════════
// New code should use HabitateTheme.colors.* from the CompositionLocal.
// These aliases exist only for backward compatibility during migration.
// ═══════════════════════════════════════════════════════════════════════════

@Deprecated("Use HabitateTheme.colors.success", ReplaceWith("SemanticSuccess", "com.ninety5.habitate.ui.theme.SemanticSuccess"))
val SageGreen = SemanticSuccess

// ═══════════════════════════════════════════════════════════════════════════
// BRAND CONSTANTS — Use HabitateTheme.colors.primary / .onPrimary instead
// ═══════════════════════════════════════════════════════════════════════════

@Deprecated("Use HabitateTheme.colors.primary (= Primary500)", ReplaceWith("Primary500", "com.ninety5.habitate.ui.theme.Primary500"))
val HabitateDarkGreenStart = Color(0xFF1F3D32)

@Deprecated("Use Primary800 / BrandDeepForest", ReplaceWith("Primary800", "com.ninety5.habitate.ui.theme.Primary800"))
val HabitateDarkGreenEnd = Color(0xFF0F1C18)

@Deprecated("Use HabitateTheme.colors.textInverse or BrandCream", ReplaceWith("BrandCream", "com.ninety5.habitate.ui.theme.BrandCream"))
val HabitateOffWhite = Color(0xFFF2EFEA)

@Deprecated("Use MaterialTheme.colorScheme.primary or a token color", ReplaceWith("MaterialTheme.colorScheme.primary"))
val SoftIndigo = Color(0xFF5C6BC0)

val SoftRed = Color(0xFFEF5350)

val MutedLilac = Color(0xFF9575CD)
