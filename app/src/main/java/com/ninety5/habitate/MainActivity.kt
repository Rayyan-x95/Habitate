package com.ninety5.habitate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ninety5.habitate.ui.navigation.HabitateNavHost
import com.ninety5.habitate.ui.navigation.Screen
import com.ninety5.habitate.ui.navigation.bottomNavItems
import com.ninety5.habitate.ui.screens.auth.AuthViewModel
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.SoftIndigo
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.ninety5.habitate.data.repository.ChatRepository
import com.ninety5.habitate.ui.screens.settings.SettingsViewModel
import com.ninety5.habitate.util.FeatureFlags
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var featureFlags: FeatureFlags

    @Inject
    lateinit var chatRepository: ChatRepository

    private val authViewModel: AuthViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        lifecycleScope.launch {
            authViewModel.uiState.collect { state ->
                if (state.isLoggedIn) {
                    launch { chatRepository.initializeRealtime() }
                }
            }
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val darkTheme = when (settingsState.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            HabitateTheme(darkTheme = darkTheme) {
                HabitateApp(authViewModel, featureFlags)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.toString()?.let { link ->
            authViewModel.handleDeepLink(link)
        }
    }
}

@Composable
fun HabitateApp(viewModel: AuthViewModel, featureFlags: FeatureFlags) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val uiState by viewModel.uiState.collectAsState()

    // Screens that should show bottom navigation
    val bottomNavScreens = mutableListOf(
        Screen.Feed.route,
        Screen.Habitats.route,
        Screen.Create.route,
        Screen.Activity.route,
        Screen.Profile.route
    )

    if (featureFlags.isFocusModeEnabled) {
        bottomNavScreens.add(Screen.Focus.route)
    }

    val showBottomBar = currentDestination?.route in bottomNavScreens

    val startDestination = when {
        !uiState.isLoggedIn -> Screen.Login.route
        !uiState.isOnboarded -> Screen.Onboarding.route
        else -> Screen.Feed.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                HabitateBottomNavigation(
                    currentRoute = currentDestination?.route,
                    featureFlags = featureFlags,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        HabitateNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination
        )
    }
}

@Composable
fun HabitateBottomNavigation(
    currentRoute: String?,
    featureFlags: FeatureFlags,
    onNavigate: (Screen) -> Unit
) {
    val items = androidx.compose.runtime.remember(featureFlags) {
        bottomNavItems.filter { item ->
            when (item.screen) {
                Screen.Focus -> featureFlags.isFocusModeEnabled
                else -> true
            }
        }
    }

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoftIndigo,
                    selectedTextColor = SoftIndigo,
                    indicatorColor = SoftIndigo.copy(alpha = 0.1f)
                )
            )
        }
    }
}
