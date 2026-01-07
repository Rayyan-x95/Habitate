package com.ninety5.habitate.ui.screens.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.ui.components.*
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateLoadingScreen
import com.ninety5.habitate.ui.navigation.Screen

/**
 * Habit List Screen - Main hub for viewing and managing habits.
 * 
 * Features:
 * - List of all active habits
 * - Quick completion from list
 * - Category filtering
 * - Search functionality
 * - Empty/Error/Offline states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClose = { showSearchBar = false }
                )
            } else {
                TopAppBar(
                    title = { Text("My Habits", color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Rounded.Search, "Search")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.HabitCreate.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, "Create habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            when {
                uiState.isLoading -> {
                    HabitateLoadingScreen()
                }
                
                uiState.habits.isEmpty() && searchQuery.isBlank() && selectedCategory == null -> {
                    HabitateEmptyState(
                        title = "No habits yet",
                        description = "Create your first habit to start building better routines",
                        icon = Icons.Rounded.SelfImprovement,
                        actionText = "Create Habit",
                        onAction = { navController.navigate(Screen.HabitCreate.route) }
                    )
                }
                
                uiState.habits.isEmpty() -> {
                    HabitateEmptyState(
                        title = "No habits found",
                        description = "Try adjusting your filters or search query",
                        icon = Icons.Rounded.SearchOff
                    )
                }
                
                else -> {
                    Column {
                        // Category filters
                        CategoryFilters(
                            selectedCategory = selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected
                        )
                        
                        // Habit list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.habits,
                                key = { it.habit.id }
                            ) { habitWithStreak ->
                                val isCompleted by viewModel.isCompletedToday(habitWithStreak.habit.id)
                                    .collectAsState(initial = false)
                                
                                HabitCard(
                                    habit = habitWithStreak.habit,
                                    streak = habitWithStreak.streak,
                                    isCompletedToday = isCompleted,
                                    onComplete = {
                                        viewModel.completeHabit(habitWithStreak.habit.id)
                                    },
                                    onClick = {
                                        navController.navigate(
                                            Screen.HabitDetail.createRoute(habitWithStreak.habit.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilters(
    selectedCategory: HabitCategory?,
    onCategorySelected: (HabitCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" filter
        item {
            CategoryChip(
                categoryName = "All",
                color = MaterialTheme.colorScheme.primary,
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        
        // Category filters
        items(HabitCategory.entries) { category ->
            val color = try {
                Color(android.graphics.Color.parseColor(category.getColor()))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }
            
            CategoryChip(
                categoryName = category.getDisplayName(),
                color = color,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search habits...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Close search")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, "Clear")
                }
            }
        },
        modifier = modifier
    )
}
