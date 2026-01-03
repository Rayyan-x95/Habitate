package com.ninety5.habitate.ui.components.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.*

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                    HABITATE COMPONENT LIBRARY - NAVIGATION               ║
 * ║                                                                          ║
 * ║  Bottom nav with glass effect and centered FAB                           ║
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
        route = "habitats",
        label = "Habitats",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    ),
    // Create button is special - handled separately
    HabitateNavItem(
        route = "activity",
        label = "Activity",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    ),
    HabitateNavItem(
        route = "profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Premium bottom navigation with glass effect and centered FAB.
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
        modifier = modifier.fillMaxWidth()
    ) {
        // Glass background
        GlassNavBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(Size.bottomNavHeight)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First two items
                items.take(2).forEach { item ->
                    HabitateNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
                
                // Spacer for FAB
                Spacer(Modifier.width(Size.fabMedium + Spacing.lg))
                
                // Last two items
                items.drop(2).forEach { item ->
                    HabitateNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
        
        // Centered FAB
        HabitateCreateFab(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )
    }
}

/**
 * Individual nav item with animation.
 */
@Composable
private fun HabitateNavItem(
    item: HabitateNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = HabitateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.textMuted,
        label = "navIconColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(),
        label = "navItemScale"
    )
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(Spacing.sm)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with optional badge
        Box {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(Size.iconLg),
                tint = iconColor
            )
            
            if (item.badge != null && item.badge > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    containerColor = colors.accent
                ) {
                    Text(
                        text = if (item.badge > 99) "99+" else item.badge.toString(),
                        style = CaptionText
                    )
                }
            }
        }
        
        Spacer(Modifier.height(Spacing.xxs))
        
        Text(
            text = item.label,
            style = CaptionText,
            color = iconColor
        )
        
        // Selection indicator
        val indicatorWidth by animateDpAsState(
            targetValue = if (isSelected) 20.dp else 0.dp,
            label = "navIndicator"
        )
        
        Spacer(Modifier.height(Spacing.xxs))
        
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(colors.primary)
        )
    }
}

/**
 * Centered create FAB for bottom nav.
 */
@Composable
private fun HabitateCreateFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(Size.fabMedium),
        containerColor = colors.fabBackground,
        contentColor = colors.fabContent,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Elevation.md,
            pressedElevation = Elevation.sm
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create",
            modifier = Modifier.size(Size.iconLg)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TOP APP BAR
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standard top app bar with optional navigation and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val colors = HabitateTheme.colors
    
    TopAppBar(
        title = {
            Text(
                text = title,
                style = SectionTitle
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
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
 * Large top app bar for feature screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitateLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val colors = HabitateTheme.colors
    
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                style = ScreenTitle
            )
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

// ═══════════════════════════════════════════════════════════════════════════
// BACK BUTTON
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun HabitateBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = colors.textPrimary
        )
    }
}

@Composable
fun HabitateCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors
    
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = colors.textPrimary
        )
    }
}
