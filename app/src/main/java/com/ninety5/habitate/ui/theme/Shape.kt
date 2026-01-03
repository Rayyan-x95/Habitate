package com.ninety5.habitate.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - SHAPES                       ║
 * ║                                                                          ║
 * ║  Rounded corners everywhere (12-20dp)                                    ║
 * ║  Following Material 3 + Apple-inspired minimalism                        ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// MATERIAL 3 SHAPES
// ═══════════════════════════════════════════════════════════════════════════

val Shapes = Shapes(
    // Tags, tiny chips, status badges
    extraSmall = RoundedCornerShape(Radius.xs),
    
    // Buttons, text fields, small cards
    small = RoundedCornerShape(Radius.sm),
    
    // Standard cards, dialogs, menus
    medium = RoundedCornerShape(Radius.md),
    
    // Featured cards, large dialogs
    large = RoundedCornerShape(Radius.lg),
    
    // Bottom sheets, full-screen dialogs
    extraLarge = RoundedCornerShape(Radius.xxl)
)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC SHAPE ALIASES
// ═══════════════════════════════════════════════════════════════════════════

/** Pill-shaped buttons (full rounded) */
val PillShape = RoundedCornerShape(Radius.pill)

/** Standard button shape */
val ButtonShape = RoundedCornerShape(Radius.sm)

/** Primary CTA button (more rounded) */
val PrimaryButtonShape = RoundedCornerShape(Radius.md)

/** Card shape */
val CardShape = RoundedCornerShape(Radius.md)

/** Featured/highlighted card */
val FeaturedCardShape = RoundedCornerShape(Radius.lg)

/** Input field shape */
val InputShape = RoundedCornerShape(Radius.sm)

/** Chip/tag shape */
val ChipShape = RoundedCornerShape(Radius.pill)

/** Dialog shape */
val DialogShape = RoundedCornerShape(Radius.xl)

/** Bottom sheet shape */
val BottomSheetShape = RoundedCornerShape(
    topStart = Radius.xxl,
    topEnd = Radius.xxl,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** Top navigation bar shape */
val TopBarShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = Radius.lg,
    bottomEnd = Radius.lg
)

/** Avatar shape */
val AvatarShape: Shape = CircleShape

/** Image shape in cards */
val ImageShape = RoundedCornerShape(Radius.md)

/** FAB shape */
val FabShape = RoundedCornerShape(Radius.lg)

/** Extended FAB shape */
val ExtendedFabShape = RoundedCornerShape(Radius.lg)

/** Snackbar shape */
val SnackbarShape = RoundedCornerShape(Radius.md)

/** Tooltip shape */
val TooltipShape = RoundedCornerShape(Radius.sm)
