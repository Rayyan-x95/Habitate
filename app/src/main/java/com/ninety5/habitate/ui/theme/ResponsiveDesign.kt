package com.ninety5.habitate.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                      RESPONSIVE DESIGN SYSTEM                             ║
 * ║                                                                          ║
 * ║  Phase 2 enhancements for better responsiveness:                           ║
 * ║  • Adaptive spacing based on screen size                                 ║
 * ║  • Responsive typography scaling                                        ║
 * ║  • Device-specific layout adjustments                                   ║
 * ║  • Safe area handling                                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Device size categories
 */
enum class DeviceSize {
    COMPACT,   // Small phones (< 360dp width)
    MEDIUM,    // Standard phones (360-600dp)
    EXPANDED,  // Large phones/tablets (600-840dp)
    LARGE      // Tablets (> 840dp)
}

/**
 * Responsive spacing tokens
 */
data class ResponsiveSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp
)

/**
 * Responsive typography tokens
 */
data class ResponsiveTypography(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp
)

/**
 * Responsive design tokens
 */
data class ResponsiveTokens(
    val spacing: ResponsiveSpacing,
    val typography: ResponsiveTypography,
    val deviceSize: DeviceSize
)

/**
 * Default responsive tokens for compact devices
 */
private val CompactTokens = ResponsiveTokens(
    spacing = ResponsiveSpacing(
        xs = 2.dp,
        sm = 4.dp,
        md = 8.dp,
        lg = 12.dp,
        xl = 16.dp,
        xxl = 24.dp
    ),
    typography = ResponsiveTypography(
        xs = 10.dp,
        sm = 12.dp,
        md = 14.dp,
        lg = 16.dp,
        xl = 20.dp,
        xxl = 24.dp
    ),
    deviceSize = DeviceSize.COMPACT
)

/**
 * Default responsive tokens for medium devices
 */
private val MediumTokens = ResponsiveTokens(
    spacing = ResponsiveSpacing(
        xs = 4.dp,
        sm = 8.dp,
        md = 16.dp,
        lg = 24.dp,
        xl = 32.dp,
        xxl = 48.dp
    ),
    typography = ResponsiveTypography(
        xs = 10.dp,
        sm = 12.dp,
        md = 14.dp,
        lg = 16.dp,
        xl = 20.dp,
        xxl = 32.dp
    ),
    deviceSize = DeviceSize.MEDIUM
)

/**
 * Default responsive tokens for expanded devices
 */
private val ExpandedTokens = ResponsiveTokens(
    spacing = ResponsiveSpacing(
        xs = 4.dp,
        sm = 8.dp,
        md = 16.dp,
        lg = 32.dp,
        xl = 48.dp,
        xxl = 64.dp
    ),
    typography = ResponsiveTypography(
        xs = 12.dp,
        sm = 14.dp,
        md = 16.dp,
        lg = 18.dp,
        xl = 24.dp,
        xxl = 36.dp
    ),
    deviceSize = DeviceSize.EXPANDED
)

/**
 * Default responsive tokens for large devices
 */
private val LargeTokens = ResponsiveTokens(
    spacing = ResponsiveSpacing(
        xs = 8.dp,
        sm = 16.dp,
        md = 24.dp,
        lg = 48.dp,
        xl = 64.dp,
        xxl = 96.dp
    ),
    typography = ResponsiveTypography(
        xs = 14.dp,
        sm = 16.dp,
        md = 18.dp,
        lg = 20.dp,
        xl = 28.dp,
        xxl = 40.dp
    ),
    deviceSize = DeviceSize.LARGE
)

/**
 * Get responsive tokens based on current configuration
 */
@Composable
@ReadOnlyComposable
fun responsiveTokens(): ResponsiveTokens {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 360.dp -> CompactTokens
        screenWidth < 600.dp -> MediumTokens
        screenWidth < 840.dp -> ExpandedTokens
        else -> LargeTokens
    }
}

/**
 * Safe area insets for edge-to-edge display
 */
@Composable
fun safeAreaInsets() = WindowInsets.systemBars

/**
 * Helper function to get responsive spacing
 */
@Composable
@ReadOnlyComposable
fun responsiveSpacing(): ResponsiveSpacing = responsiveTokens().spacing

/**
 * Helper function to get responsive typography
 */
@Composable
@ReadOnlyComposable
fun responsiveTypography(): ResponsiveTypography = responsiveTokens().typography

/**
 * Helper function to get device size
 */
@Composable
@ReadOnlyComposable
fun deviceSize(): DeviceSize = responsiveTokens().deviceSize

/**
 * Extension functions for easy access to responsive values
 */
@Composable
@ReadOnlyComposable
fun Int.responsiveDp(): Dp = when (this) {
    0 -> responsiveSpacing().xs
    1 -> responsiveSpacing().sm
    2 -> responsiveSpacing().md
    3 -> responsiveSpacing().lg
    4 -> responsiveSpacing().xl
    5 -> responsiveSpacing().xxl
    else -> this.dp
}

/**
 * Responsive font size extension
 */
@Composable
@ReadOnlyComposable
fun Int.responsiveSp(): androidx.compose.ui.unit.TextUnit = when (this) {
    0 -> responsiveTypography().xs.value.sp
    1 -> responsiveTypography().sm.value.sp
    2 -> responsiveTypography().md.value.sp
    3 -> responsiveTypography().lg.value.sp
    4 -> responsiveTypography().xl.value.sp
    5 -> responsiveTypography().xxl.value.sp
    else -> this.sp
}

/**
 * Check if device is compact (small phone)
 */
@Composable
@ReadOnlyComposable
fun isCompactDevice(): Boolean = deviceSize() == DeviceSize.COMPACT

/**
 * Check if device is medium (standard phone)
 */
@Composable
@ReadOnlyComposable
fun isMediumDevice(): Boolean = deviceSize() == DeviceSize.MEDIUM

/**
 * Check if device is tablet-sized
 */
@Composable
@ReadOnlyComposable
fun isTabletDevice(): Boolean = deviceSize() in listOf(DeviceSize.EXPANDED, DeviceSize.LARGE)

/**
 * Get responsive padding for cards
 */
@Composable
@ReadOnlyComposable
fun responsiveCardPadding(): Dp = when (deviceSize()) {
    DeviceSize.COMPACT -> 12.dp
    DeviceSize.MEDIUM -> 16.dp
    DeviceSize.EXPANDED -> 20.dp
    DeviceSize.LARGE -> 24.dp
}

/**
 * Get responsive content padding for screens
 */
@Composable
@ReadOnlyComposable
fun responsiveContentPadding(): Dp = when (deviceSize()) {
    DeviceSize.COMPACT -> 8.dp
    DeviceSize.MEDIUM -> 16.dp
    DeviceSize.EXPANDED -> 24.dp
    DeviceSize.LARGE -> 32.dp
}

/**
 * Get responsive button height
 */
@Composable
@ReadOnlyComposable
fun responsiveButtonHeight(): Dp = when (deviceSize()) {
    DeviceSize.COMPACT -> 40.dp
    DeviceSize.MEDIUM -> 48.dp
    DeviceSize.EXPANDED -> 52.dp
    DeviceSize.LARGE -> 56.dp
}

/**
 * Get responsive avatar size
 */
@Composable
@ReadOnlyComposable
fun responsiveAvatarSize(): Dp = when (deviceSize()) {
    DeviceSize.COMPACT -> 32.dp
    DeviceSize.MEDIUM -> 40.dp
    DeviceSize.EXPANDED -> 48.dp
    DeviceSize.LARGE -> 56.dp
}
