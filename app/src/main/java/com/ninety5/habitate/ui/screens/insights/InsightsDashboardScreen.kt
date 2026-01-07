package com.ninety5.habitate.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ninety5.habitate.data.local.entity.InsightEntity
import com.ninety5.habitate.data.local.entity.InsightPriority
import com.ninety5.habitate.data.local.entity.InsightType
import com.ninety5.habitate.ui.components.ExperimentalFeatureBanner
import com.ninety5.habitate.ui.screens.insights.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsDashboardScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val insights by viewModel.insights.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Insights") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ExperimentalFeatureBanner(
                featureName = "AI Insights",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (insights.isEmpty()) {
                    item {
                        Text(
                            text = "No insights available yet. Keep using the app to generate insights!",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(insights, key = { it.id }) { insight ->
                        InsightCard(
                            insight = insight,
                            onDismiss = { viewModel.dismissInsight(insight) },
                            onAction = { viewModel.markAsActioned(insight) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: InsightEntity,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val containerColor = when (insight.priority) {
        InsightPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
        InsightPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
        InsightPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when (insight.type) {
        InsightType.STREAK_RISK -> Icons.Default.Warning
        InsightType.MILESTONE_APPROACHING -> Icons.AutoMirrored.Filled.TrendingUp
        InsightType.PATTERN_DETECTED -> Icons.Default.Lightbulb
        InsightType.SUGGESTION -> Icons.Default.Lightbulb
        InsightType.MOOD_CORRELATION -> Icons.Default.Lightbulb
        InsightType.WEEKLY_SUMMARY -> Icons.AutoMirrored.Filled.TrendingUp
        InsightType.TASK_FAILURE -> Icons.Default.Warning
        InsightType.HABIT_FRICTION -> Icons.Default.Warning
        InsightType.ENERGY_TREND -> Icons.Default.Lightbulb
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                }
                IconButton(onClick = onAction) {
                    Icon(Icons.Default.Check, contentDescription = "Action")
                }
            }
        }
    }
}
