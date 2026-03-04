package com.ninety5.habitate.ui.screens.create

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun CreateScreen(
    onCreatePost: () -> Unit,
    onCreateTask: () -> Unit,
    onCreateHabit: () -> Unit,
    onCreateWorkout: () -> Unit,
    onCreateHabitat: () -> Unit,
    onPlannerClick: () -> Unit
) {
    val colors = HabitateTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xxl, bottom = 96.dp)
        ) {
            Text(
                text = "Create",
                style = HabitateTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = "What would you like to create?",
                style = HabitateTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            val options = listOf(
                CreateOption("Post", Icons.Rounded.Edit, "Share your thoughts", onCreatePost),
                CreateOption("Task", Icons.Rounded.CheckCircle, "To-dos & Reminders", onCreateTask),
                CreateOption("Habit", Icons.Rounded.CheckCircle, "Build daily routines", onCreateHabit),
                CreateOption("Workout", Icons.Rounded.FitnessCenter, "Log your activity", onCreateWorkout),
                CreateOption("Habitat", Icons.Rounded.Group, "Start a community", onCreateHabitat),
                CreateOption("AI Plan", Icons.Rounded.SmartToy, "Generate daily plan", onPlannerClick)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                itemsIndexed(options) { index, option ->
                    // Staggered entrance animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 60L)
                        visible = true
                    }
                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(300),
                        label = "card_alpha"
                    )

                    CreateOptionCard(
                        option = option,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            }
        }
    }
}

data class CreateOption(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun CreateOptionCard(
    option: CreateOption,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors

    Card(
        onClick = option.onClick,
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.md)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(Size.iconXl)
                    .background(
                        color = colors.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(Radius.md)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(Size.iconLg)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = option.title,
                style = HabitateTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = option.description,
                style = HabitateTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}








