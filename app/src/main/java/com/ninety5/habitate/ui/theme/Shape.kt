package com.ninety5.habitate.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE DESIGN SYSTEM - SHAPES                       ║
 * ║                         Version 3.0 - 2026 Spatial UI                    ║
 * ║                                                                          ║
 * ║  Philosophy:                                                              ║
 * ║  • Consistent rounded corners (16-20dp primary range for cards)          ║
 * ║  • Softer, more approachable shapes                                      ║
 * ║  • Pill shapes for interactive elements                                  ║
 * ║                                                                          ║
 * ║  Inspiration: VisionOS, Apple Health, Linear                             ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// MATERIAL 3 SHAPES (Refined for 2026 Standard)
// ═══════════════════════════════════════════════════════════════════════════

val Shapes = Shapes(
    // Tags, tiny chips, status badges
    extraSmall = RoundedCornerShape(8.dp),
    
    // Buttons, text fields, small cards
    small = RoundedCornerShape(12.dp),
    
    // Standard cards, dialogs, menus (16-20dp range)
    medium = RoundedCornerShape(20.dp),
    
    // Featured cards, large dialogs
    large = RoundedCornerShape(24.dp),
    
    // Bottom sheets, full-screen dialogs
    extraLarge = RoundedCornerShape(32.dp)
)

// ═══════════════════════════════════════════════════════════════════════════
// SEMANTIC SHAPE ALIASES (Use these for consistency)
// ═══════════════════════════════════════════════════════════════════════════

/** Pill-shaped buttons, tags (full rounded) */
val PillShape = CircleShape

/** Standard button shape - Fully rounded for modern feel */
val ButtonShape = CircleShape

/** Primary CTA button - Fully rounded */
val PrimaryButtonShape = CircleShape

/** Standard card shape (20dp) */
val CardShape = RoundedCornerShape(20.dp)

/** Featured/highlighted card - More prominent (24dp) */
val FeaturedCardShape = RoundedCornerShape(24.dp)

/** Compact card (list items) (16dp) */
val CardShapeCompact = RoundedCornerShape(16.dp)

/** Input field shape (16dp) */
val InputShape = RoundedCornerShape(16.dp)

/** Chip/tag shape - Pill-like */
val ChipShape = CircleShape

/** Dialog shape (24dp) */
val DialogShape = RoundedCornerShape(24.dp)

/** Bottom sheet shape - Rounded top corners only (32dp) */
val BottomSheetShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** Modal sheet shape - Slightly smaller radius (24dp) */
val ModalSheetShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** Avatar shape - Perfect circle */
val AvatarShape: Shape = CircleShape

/** Image shape in cards (16dp) */
val ImageShape = RoundedCornerShape(16.dp)

/** Image shape compact (12dp) */
val ImageShapeCompact = RoundedCornerShape(12.dp)

/** FAB shape - Fully rounded */
val FabShape = CircleShape

/** Search bar shape - Pill / fully rounded */
val SearchBarShape = CircleShape

/** Navigation indicator shape - Pill-shaped selection indicator */
val NavIndicatorShape = CircleShape

/** Extended FAB shape - Fully rounded */
val ExtendedFabShape = CircleShape
