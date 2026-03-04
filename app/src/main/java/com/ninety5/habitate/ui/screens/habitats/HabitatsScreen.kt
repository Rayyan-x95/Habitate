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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ninety5.habitate.domain.model.HabitatPrivacy
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateFab
import com.ninety5.habitate.ui.components.designsystem.HabitateLargeTopBar
import com.ninety5.habitate.ui.components.designsystem.HabitatePrimaryButton
import com.ninety5.habitate.ui.theme.HabitateTheme
import com.ninety5.habitate.ui.theme.Radius
import com.ninety5.habitate.ui.theme.Size
import com.ninety5.habitate.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitatsScreen(
    onHabitatClick: (String) -> Unit,
    onCreateHabitat: () -> Unit,
    viewModel: HabitatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = HabitateTheme.colors
    var selectedFilter by remember { mutableStateOf(HabitatFilter.ALL) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val filteredMyHabitats = remember(uiState.myHabitats, selectedFilter) {
        if (selectedFilter == HabitatFilter.ALL) uiState.myHabitats
        else uiState.myHabitats.filter { it.category == selectedFilter }
    }
    val filteredDiscoverHabitats = remember(uiState.discoverHabitats, selectedFilter) {
        if (selectedFilter == HabitatFilter.ALL) uiState.discoverHabitats
        else uiState.discoverHabitats.filter { it.category == selectedFilter }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HabitateLargeTopBar(
                title = "Habitats",
                scrollBehavior = scrollBehavior
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Filter chips
                item(key = "filters") {
                    FilterChipsRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }

                // My Habitats section
                item(key = "my_header") {
                    SectionHeader(
                        title = "My Habitats",
                        count = filteredMyHabitats.size
                    )
                }

                if (filteredMyHabitats.isEmpty()) {
                    item(key = "empty") {
                        EmptyHabitatsState(onCreateHabitat = onCreateHabitat)
                    }
                } else {
                    items(filteredMyHabitats, key = { it.id }) { habitat ->
                        HabitatCard(
                            habitat = habitat,
                            onClick = { onHabitatClick(habitat.id) }
                        )
                    }
                }

                // Discover section
                item(key = "discover_header") {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    SectionHeader(
                        title = "Discover",
                        count = filteredDiscoverHabitats.size
                    )
                }

                items(filteredDiscoverHabitats, key = { it.id }) { habitat ->
                    HabitatCard(
                        habitat = habitat,
                        onClick = { onHabitatClick(habitat.id) }
                    )
                }
            }
        }

        // FAB
        HabitateFab(
            icon = Icons.Rounded.Add,
            contentDescription = "Create habitat",
            onClick = onCreateHabitat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.screenHorizontal, bottom = 96.dp)
        )
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: HabitatFilter,
    onFilterSelected: (HabitatFilter) -> Unit
) {
    val colors = HabitateTheme.colors

    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(HabitatFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        filter.displayName,
                        style = HabitateTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary,
                    containerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = colors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(Radius.md)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    val colors = HabitateTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = HabitateTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )
        if (count > 0) {
            Surface(
                color = colors.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    style = HabitateTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyHabitatsState(onCreateHabitat: () -> Unit) {
    val colors = HabitateTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .background(
                colors.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(Radius.md)
            )
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Size.iconXl)
                .background(colors.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(Size.iconLg),
                tint = colors.primary
            )
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Join or Create a Habitat",
            style = HabitateTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        Text(
            text = "Connect with others who share your interests.",
            style = HabitateTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xxs, bottom = Spacing.md)
        )
        HabitatePrimaryButton(
            text = "Create New Habitat",
            onClick = onCreateHabitat
        )
    }
}

@Composable
fun HabitatCard(
    habitat: HabitatUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabitateTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxs)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(0.5.dp, colors.borderSubtle.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habitat image
            Surface(
                modifier = Modifier
                    .size(Size.avatarLg)
                    .clip(RoundedCornerShape(Radius.md)),
                color = colors.primary.copy(alpha = 0.08f)
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
                            modifier = Modifier.size(Size.iconLg),
                            tint = colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Habitat info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habitat.name,
                        style = HabitateTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                        color = colors.onSurface
                    )
                    if (habitat.privacy != HabitatPrivacy.PUBLIC) {
                        Spacer(modifier = Modifier.width(Spacing.xxs))
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Private",
                            modifier = Modifier.size(14.dp),
                            tint = colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Text(
                    text = habitat.description,
                    style = HabitateTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = Spacing.xxs)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${habitat.memberCount} members",
                        style = HabitateTheme.typography.labelMedium,
                        color = colors.primary
                    )

                    if (habitat.activeChallenge != null) {
                        Text(
                            text = " · ${habitat.activeChallenge}",
                            style = HabitateTheme.typography.labelMedium,
                            color = colors.primary.copy(alpha = 0.7f),
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
    val isJoined: Boolean,
    val category: HabitatFilter? = null
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