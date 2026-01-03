package com.ninety5.habitate.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.ui.viewmodel.TimelineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ninety5.habitate.ui.viewmodel.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val timelineItems = viewModel.timelineItems.collectAsLazyPagingItems()
    val currentFilter by viewModel.filterType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportTimeline(it, ExportFormat.JSON) }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.exportTimeline(it, ExportFormat.PDF) }
    }

    Scaffold(
        topBar = {
            Column {
                if (isSearchActive) {
                    SearchBar(
                        query = searchQuery ?: "",
                        onQueryChange = viewModel::setSearchQuery,
                        onSearch = { isSearchActive = false },
                        active = true,
                        onActiveChange = { isSearchActive = it },
                        placeholder = { Text("Search timeline...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false 
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                            }
                        }
                    ) {}
                } else {
                    TopAppBar(
                        title = { Text("Life Timeline") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export as JSON") },
                                    onClick = {
                                        showMenu = false
                                        exportJsonLauncher.launch("habitate_timeline.json")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as PDF") },
                                    onClick = {
                                        showMenu = false
                                        exportPdfLauncher.launch("habitate_timeline.pdf")
                                    }
                                )
                            }
                        }
                    )
                }
                TimelineFilterRow(
                    currentFilter = currentFilter,
                    onFilterSelected = viewModel::setFilter
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = timelineItems.itemCount,
                key = timelineItems.itemKey { it.id },
                contentType = timelineItems.itemContentType { it.type }
            ) { index ->
                val item = timelineItems[index]
                if (item != null) {
                    TimelineItemCard(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineFilterRow(
    currentFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    val filters = listOf(
        "All" to null,
        "Posts" to "post",
        "Workouts" to "workout",
        "Stories" to "story",
        "Tasks" to "task",
        "Insights" to "insight",
        "Journal" to "journal"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (label, type) ->
            val selected = currentFilter == type
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(type) },
                label = { Text(label) },
                leadingIcon = if (selected) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
    }
}

@Composable
fun TimelineItemCard(item: TimelineItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(getTypeColor(item.type))
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp) // Min height
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        // Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getTypeIcon(item.type),
                        contentDescription = null,
                        tint = getTypeColor(item.type),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title ?: "Untitled",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (item.subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getTypeColor(type: String): Color {
    return when (type) {
        "post" -> Color(0xFF6366F1) // Indigo
        "workout" -> Color(0xFFEF4444) // Red
        "story" -> Color(0xFFF59E0B) // Amber
        "task" -> Color(0xFF10B981) // Emerald
        "insight" -> Color(0xFF8B5CF6) // Purple
        "journal" -> Color(0xFFEC4899) // Pink
        else -> Color.Gray
    }
}

fun getTypeIcon(type: String): ImageVector {
    return when (type) {
        "post" -> Icons.Default.Edit
        "workout" -> Icons.Default.FitnessCenter
        "story" -> Icons.Default.History
        "task" -> Icons.Default.CheckCircle
        "insight" -> Icons.Default.Lightbulb
        "journal" -> Icons.Default.Edit // Reusing Edit for now, or Book if I import it
        else -> Icons.Default.History
    }
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
}
