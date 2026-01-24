package com.ninety5.habitate.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - SHAPES                       ║
 * ║                         Version 2.0 - Minimal Redesign                   ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Consistent rounded corners (8-16dp primary range)                     ║
 * ║  • Softer, more approachable shapes                                      ║
 * ║  • Pill shapes for interactive elements                                  ║
 * ║                                                                          ║
 * ║  Inspiration: Apple Health, Linear, Notion                               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// MATERIAL 3 SHAPES (Refined)
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
// SEMANTIC SHAPE ALIASES (Use these for consistency)
// ═══════════════════════════════════════════════════════════════════════════

/** Pill-shaped buttons, tags (full rounded) */
val PillShape = RoundedCornerShape(Radius.pill)

/** Standard button shape - Slightly rounded */
val ButtonShape = RoundedCornerShape(Radius.sm)

/** Primary CTA button - More prominent rounding */
val PrimaryButtonShape = RoundedCornerShape(Radius.md)

/** Standard card shape */
val CardShape = RoundedCornerShape(Radius.md)

/** Featured/highlighted card - More prominent */
val FeaturedCardShape = RoundedCornerShape(Radius.lg)

/** Compact card (list items) */
val CardShapeCompact = RoundedCornerShape(Radius.sm)

/** Input field shape */
val InputShape = RoundedCornerShape(Radius.sm)

/** Chip/tag shape - Pill-like */
val ChipShape = RoundedCornerShape(Radius.pill)

/** Dialog shape */
val DialogShape = RoundedCornerShape(Radius.lg)

/** Bottom sheet shape - Rounded top corners only */
val BottomSheetShape = RoundedCornerShape(
    topStart = Radius.xxl,
    topEnd = Radius.xxl,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** Modal sheet shape - Slightly smaller radius */
val ModalSheetShape = RoundedCornerShape(
    topStart = Radius.xl,
    topEnd = Radius.xl,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** Avatar shape - Perfect circle */
val AvatarShape: Shape = CircleShape

/** Image shape in cards */
val ImageShape = RoundedCornerShape(Radius.md)

/** Image shape compact */
val ImageShapeCompact = RoundedCornerShape(Radius.sm)

/** FAB shape - More rounded for floating appearance */
val FabShape = RoundedCornerShape(Radius.lg)

/** Extended FAB shape */
val ExtendedFabShape = RoundedCornerShape(Radius.lg)

/** Snackbar shape */
val SnackbarShape = RoundedCornerShape(Radius.sm)

/** Tooltip shape */
val TooltipShape = RoundedCornerShape(Radius.xs)

/** Navigation bar indicator shape */
val NavIndicatorShape = RoundedCornerShape(Radius.pill)

/** Search bar shape - Medium rounded */
val SearchBarShape = RoundedCornerShape(Radius.md)

/** Story ring shape */
val StoryRingShape: Shape = CircleShape

/** Menu/dropdown shape */
val MenuShape = RoundedCornerShape(Radius.md)
