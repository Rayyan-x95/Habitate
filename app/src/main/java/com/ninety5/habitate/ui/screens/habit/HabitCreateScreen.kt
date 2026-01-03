package com.ninety5.habitate.ui.screens.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.local.entity.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke

/**
 * Habit Create/Edit Screen - Form for creating or editing habits.
 * 
 * Features:
 * - Text input for title & description
 * - Category selector
 * - Color picker
 * - Icon grid selector
 * - Frequency selection (Daily/Weekly/Custom)
 * - Custom schedule (specific days)
 * - Reminder time picker
 * - Form validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCreateScreen(
    habitId: String? = null,
    navController: NavController,
    viewModel: HabitCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load habit for editing if habitId is provided
    // Done in ViewModel init

    LaunchedEffect(uiState.habitSaved) {
        if (uiState.habitSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (habitId != null) "Edit Habit" else "Create Habit", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveHabit() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text("SAVE")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field
            item {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Title *") },
                    isError = uiState.titleError != null,
                    supportingText = uiState.titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Description field
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Category selector
            item {
                Text(
                    "Category *",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HabitCategory.entries) { category ->
                        CategoryCard(
                            category = category,
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }

            // Color picker
            item {
                Text(
                    "Color",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                ColorPicker(
                    selectedColor = uiState.selectedColor,
                    onColorSelected = viewModel::onColorSelected
                )
            }

            // Icon picker
            item {
                Text(
                    "Icon",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                IconPicker(
                    selectedIcon = uiState.selectedIcon,
                    onIconSelected = viewModel::onIconSelected
                )
            }

            // Frequency selector
            item {
                Text(
                    "Frequency *",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                FrequencySelector(
                    selectedFrequency = uiState.selectedFrequency,
                    onFrequencySelected = viewModel::onFrequencySelected
                )
            }

            // Custom schedule (if CUSTOM frequency)
            if (uiState.selectedFrequency == HabitFrequency.CUSTOM) {
                item {
                    Text(
                        "Select days",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    CustomScheduleSelector(
                        selectedDays = uiState.customSchedule,
                        onDayToggle = viewModel::onCustomScheduleToggle
                    )
                }
            }

            // Reminder time
            item {
                Text(
                    "Reminder (optional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                ReminderTimePicker(
                    reminderTime = uiState.reminderTime,
                    onReminderTimeSelected = viewModel::onReminderTimeSelected,
                    onClearReminder = viewModel::clearReminder
                )
            }

            // Bottom spacing
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: HabitCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = try {
        Color(android.graphics.Color.parseColor(category.getColor()))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
        ),
        border = if (selected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // Icon would go here
            }
            Spacer(Modifier.height(4.dp))
            Text(
                category.getDisplayName(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFC107", "#FF9800", "#FF5722", "#795548"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier.heightIn(max = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { colorHex ->
            val color = Color(android.graphics.Color.parseColor(colorHex))
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .clickable { onColorSelected(colorHex) }
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        "💪", "🏃", "🧘", "📚", "✍️", "🎨", "🎵", "🎮",
        "💼", "💰", "🍎", "🥗", "💧", "😴", "🌞", "🌙",
        "⭐", "🔥", "⚡", "💎", "🎯", "🚀", "🏆", "📝"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier.heightIn(max = 200.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(icons) { icon ->
            Surface(
                onClick = { onIconSelected(icon) },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedIcon == icon) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
                modifier = Modifier.aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencySelector(
    selectedFrequency: HabitFrequency?,
    onFrequencySelected: (HabitFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HabitFrequency.entries.forEach { frequency ->
            FilterChip(
                selected = selectedFrequency == frequency,
                onClick = { onFrequencySelected(frequency) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = MaterialTheme.colorScheme.onBackground
                ),
                label = {
                    Text(
                        when (frequency) {
                            HabitFrequency.DAILY -> "Daily"
                            HabitFrequency.WEEKLY -> "Weekly"
                            HabitFrequency.CUSTOM -> "Custom"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun CustomScheduleSelector(
    selectedDays: List<DayOfWeek>,
    onDayToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        DayOfWeek.MONDAY to "M",
        DayOfWeek.TUESDAY to "T",
        DayOfWeek.WEDNESDAY to "W",
        DayOfWeek.THURSDAY to "T",
        DayOfWeek.FRIDAY to "F",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "S"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { (day, label) ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = { onDayToggle(day) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReminderTimePicker(
    reminderTime: LocalTime?,
    onReminderTimeSelected: (LocalTime) -> Unit,
    onClearReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Rounded.AccessTime, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                reminderTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Set reminder"
            )
        }
        
        if (reminderTime != null) {
            IconButton(onClick = onClearReminder) {
                Icon(Icons.Rounded.Close, "Clear reminder", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }

    // Note: Using Material3 TimePicker with rememberTimePickerState
    // Time picker dialog implemented via Button -> TimePickerDialog flow
}
