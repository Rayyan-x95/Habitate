package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - NAVIGATION               ║
 * ║                         Version 3.0 — Floating Pill Redesign             ║
 * ║                                                                          ║
 * ║  Design: Floating pill-shaped bottom bar with centered Create FAB        ║
 * ║  Minimal visual weight, smooth transitions, 48dp touch targets           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

// ═══════════════════════════════════════════════════════════════════════════
// NAVIGATION ITEMS
// ═══════════════════════════════════════════════════════════════════════════

data class HabitateNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: Int? = null
)

val DefaultNavItems = listOf(
    HabitateNavItem(
        route = "feed",
        label = "Feed",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    HabitateNavItem(
        route = "focus",
        label = "Focus",
        selectedIcon = Icons.Filled.SelfImprovement,
        unselectedIcon = Icons.Outlined.SelfImprovement
    ),
    // Create button is special - handled separately as centered FAB
    HabitateNavItem(
        route = "habitats",
        label = "Habitats",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    ),
    HabitateNavItem(
        route = "profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// FLOATING PILL BOTTOM NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Floating pill-shaped bottom navigation bar with centered Create FAB.
 * Hovers above content with subtle shadow, rounded corners, and glass tint.
 */
@Composable
fun HabitateBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<HabitateNavItem> = DefaultNavItems
) {
    val colors = HabitateTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating pill container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(Radius.pill),
            color = colors.navBarBackground,
            shadowElevation = Elevation.md,
            tonalElevation = Elevation.whisper
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (items.isNotEmpty()) {
                    val midpoint = items.size / 2

                    // Left items (before FAB)
                    items.take(midpoint).forEach { item ->
                        FloatingNavItem(
                            item = item,
                            isSelected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Center FAB space
                    Box(
                        modifier = Modifier.weight(1.2f),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingCreateFab(onClick = onCreateClick)
                    }

                    // Right items (after FAB)
                    items.drop(midpoint).forEach { item ->
                        FloatingNavItem(
                            item = item,
                            isSelected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual floating nav item with pill indicator on selection.
 */
@Composable
private fun FloatingNavItem(
    item: HabitateNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.textMuted,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "navIconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.textMuted,
        animationSpec = tween(durationMillis = Duration.fast),
        label = "navLabelColor"
    )

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = Duration.normal),
        label = "navIndicatorAlpha"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill indicator behind icon when selected
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 28.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(
                    colors.primaryContainer.copy(alpha = indicatorAlpha * 0.8f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon with optional badge
            Box {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp),
                    tint = iconColor
                )

                if (item.badge != null && item.badge > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp),
                        containerColor = colors.accent
                    ) {
                        Text(
                            text = if (item.badge > 99) "99+" else item.badge.toString(),
                            style = CaptionText
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = item.label,
            style = CaptionText,
            color = labelColor
        )
    }
}

/**
 * Centered create FAB that sits within the pill.
 * Slightly elevated above the bar with brand gradient.
 */
@Composable
private fun FloatingCreateFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .offset(y = (-8).dp),
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Elevation.sm,
            pressedElevation = Elevation.xs,
            hoveredElevation = Elevation.md
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create",
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

/**
 * Legacy bottom nav bar reference — prefer [HabitateBottomNavBar].
 */
@Deprecated("Use HabitateBottomNavBar for the floating pill design", ReplaceWith("HabitateBottomNavBar(currentRoute, onNavigate, onCreateClick, modifier, items)"))
@Composable
fun HabitateBottomNavBarLegacy(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<HabitateNavItem> = DefaultNavItems
) {
    HabitateBottomNavBar(currentRoute, onNavigate, onCreateClick, modifier, items)
}

// ═══════════════════════════════════════════════════════════════════════════
// TOP APP BAR (Calm header with clear hierarchy)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard top app bar with optional navigation and actions.
 * Clean, minimal elevation, clear title hierarchy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    transparent: Boolean = false
) {
    val colors = HabitateTheme.colors
    
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = SectionTitle
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = SupportingText,
                        color = colors.textSecondary
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (transparent) androidx.compose.ui.graphics.Color.Transparent else colors.background,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textSecondary
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Large top app bar for primary screens.
 * Generous title size, subtle collapse behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val colors = HabitateTheme.colors
    
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = ScreenTitle
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(
                        text = subtitle,
                        style = SupportingText,
                        color = colors.textSecondary
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = colors.background,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textSecondary
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Center-aligned top bar for modal or detail screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateCenterTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val colors = HabitateTheme.colors
    
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = CardTitle
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colors.background,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textSecondary
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// NAVIGATION BUTTONS (Back, Close, etc.)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard back navigation button.
 */
@Composable
fun HabitateBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Back"
) {
    val colors = HabitateTheme.colors
    
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Size.touchTarget)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

/**
 * Close button for modals and sheets.
 */
@Composable
fun HabitateCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Close"
) {
    val colors = HabitateTheme.colors
    
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Size.touchTarget)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

/**
 * More options button (three dots).
 */
@Composable
fun HabitateMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "More options"
) {
    val colors = HabitateTheme.colors
    
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Size.touchTarget)
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = contentDescription,
            tint = colors.textSecondary,
            modifier = Modifier.size(Size.iconMd)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TAB BAR (Horizontal navigation within screens)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Tab row for section navigation.
 */
@Composable
fun HabitateTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 2.dp,
                    color = colors.primary
                )
            }
        },
        divider = {
            HorizontalDivider(
                thickness = Size.dividerThickness,
                color = colors.divider
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTabIndex
            
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = if (isSelected) ButtonText else SupportingText,
                        color = if (isSelected) colors.textPrimary else colors.textMuted
                    )
                }
            )
        }
    }
}

/**
 * Scrollable tab row for many tabs.
 */
@Composable
fun HabitateScrollableTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        edgePadding = Spacing.md,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 2.dp,
                    color = colors.primary
                )
            }
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTabIndex
            
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = if (isSelected) ButtonText else SupportingText,
                        color = if (isSelected) colors.textPrimary else colors.textMuted
                    )
                }
            )
        }
    }
}
