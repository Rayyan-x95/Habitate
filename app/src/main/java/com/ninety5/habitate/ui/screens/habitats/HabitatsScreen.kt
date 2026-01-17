package com.ninety5.habitate.ui.screens.habitats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ninety5.habitate.data.local.entity.HabitatPrivacy
import com.ninety5.habitate.ui.theme.HabitateDarkGreenStart
import com.ninety5.habitate.ui.theme.HabitateOffWhite
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.SageGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitatsScreen(
    onHabitatClick: (String) -> Unit,
    onCreateHabitat: () -> Unit,
    viewModel: HabitatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HabitatFilter.ALL) }

    Scaffold(
        containerColor = HabitateDarkGreenStart,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHabitat,
                containerColor = SageGreen,
                contentColor = HabitateDarkGreenStart
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create habitat")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Filter chips
            item {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            // My Habitats section
            item {
                SectionHeader(
                    title = "My Habitats",
                    count = uiState.myHabitats.size
                )
            }

            if (uiState.myHabitats.isEmpty()) {
                item {
                    EmptyHabitatsState(onCreateHabitat = onCreateHabitat)
                }
            } else {
                items(uiState.myHabitats, key = { it.id }) { habitat ->
                    HabitatCard(
                        habitat = habitat,
                        onClick = { onHabitatClick(habitat.id) }
                    )
                }
            }

            // Discover section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Discover",
                    count = uiState.discoverHabitats.size
                )
            }

            items(uiState.discoverHabitats, key = { it.id }) { habitat ->
                HabitatCard(
                    habitat = habitat,
                    onClick = { onHabitatClick(habitat.id) }
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: HabitatFilter,
    onFilterSelected: (HabitatFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HabitatFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SageGreen,
                    selectedLabelColor = HabitateDarkGreenStart,
                    containerColor = HabitateOffWhite.copy(alpha = 0.1f),
                    labelColor = HabitateOffWhite
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HabitateOffWhite
        )
        if (count > 0) {
            Surface(
                color = SageGreen.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = SageGreen
                )
            }
        }
    }
}

@Composable
private fun EmptyHabitatsState(onCreateHabitat: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                HabitateOffWhite.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.AddCircleOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SageGreen
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Join or Create a Habitat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = HabitateOffWhite
        )
        Text(
            text = "Connect with others who share your interests.",
            style = MaterialTheme.typography.bodyMedium,
            color = HabitateOffWhite.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(
            onClick = onCreateHabitat,
            colors = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = HabitateDarkGreenStart)
        ) {
            Text("Create New Habitat")
        }
    }
}

@Composable
fun HabitatCard(
    habitat: HabitatUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HabitateDarkGreenStart.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, HabitateOffWhite.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habitat image
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = SageGreen.copy(alpha = 0.1f)
            ) {
                if (habitat.imageUrl != null) {
                    AsyncImage(
                        model = habitat.imageUrl,
                        contentDescription = habitat.name,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = SageGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Habitat info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habitat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                        color = HabitateOffWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (habitat.privacy != HabitatPrivacy.PUBLIC) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Private",
                            modifier = Modifier.size(14.dp),
                            tint = HabitateOffWhite.copy(alpha = 0.5f)
                        )
                    }
                }

                Text(
                    text = habitat.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitateOffWhite.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${habitat.memberCount} members",
                        style = MaterialTheme.typography.labelMedium,
                        color = SageGreen
                    )
                    
                    if (habitat.activeChallenge != null) {
                        Text(
                            text = " • ${habitat.activeChallenge}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

enum class HabitatFilter(val displayName: String) {
    ALL("All"),
    FITNESS("Fitness"),
    PRODUCTIVITY("Productivity"),
    WELLNESS("Wellness"),
    CREATIVE("Creative")
}

data class HabitatUiModel(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val memberCount: Int,
    val privacy: HabitatPrivacy,
    val activeChallenge: String?,
    val isJoined: Boolean
)

@Preview(showBackground = true)
@Composable
private fun HabitatCardPreview() {
    HabitateTheme {
        HabitatCard(
            habitat = HabitatUiModel(
                id = "1",
                name = "Morning Runners",
                description = "A community for early morning runners. Share your runs and motivate each other!",
                imageUrl = null,
                memberCount = 1234,
                privacy = HabitatPrivacy.PUBLIC,
                activeChallenge = "30-Day Streak",
                isJoined = true
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    HabitateTheme {
        EmptyHabitatsState(onCreateHabitat = {})
    }
}