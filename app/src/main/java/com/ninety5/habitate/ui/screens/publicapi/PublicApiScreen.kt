package com.ninety5.habitate.ui.screens.publicapi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ninety5.habitate.ui.components.ApiSectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicApiScreen(
    viewModel: PublicApiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore APIs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Weather Section
                item {
                    ApiSectionCard(
                        title = "Weather",
                        icon = Icons.Default.WbSunny,
                        action = {
                            Button(onClick = { viewModel.loadWeather(52.52, 13.41) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Berlin")
                            }
                        }
                    ) {
                        uiState.weather?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "${it.temperature}°",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Column {
                                    Text("Wind: ${it.windSpeed} km/h")
                                    Text("Lat: 52.52, Long: 13.41", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } ?: Text("Tap to load weather data", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Quote Section
                item {
                    ApiSectionCard(
                        title = "Daily Inspiration",
                        icon = Icons.Default.FormatQuote,
                        action = {
                            FilledTonalButton(onClick = { viewModel.loadRandomQuote() }) {
                                Text("New Quote")
                            }
                        }
                    ) {
                        uiState.quote?.let {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "\u201c${it.text}\u201d",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontStyle = FontStyle.Italic
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "— ${it.author}",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        } ?: Text("Need some inspiration?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Food Section
                item {
                    ApiSectionCard(
                        title = "Meal Idea",
                        icon = Icons.Default.Restaurant,
                        action = {
                            Button(onClick = { viewModel.loadRandomMeal() }) {
                                Text("Surprise Me")
                            }
                        }
                    ) {
                        uiState.meal?.let {
                            Column {
                                AsyncImage(
                                    model = it.thumbnailUrl,
                                    contentDescription = it.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(it.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                AssistChip(
                                    onClick = { },
                                    label = { Text(it.category) },
                                    leadingIcon = { Icon(Icons.Default.Category, null) }
                                )
                            }
                        } ?: Text("Hungry? Get a random meal idea.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Books Section
                item {
                    ApiSectionCard(
                        title = "Book Search",
                        icon = Icons.Default.Book,
                        action = {
                            OutlinedButton(onClick = { viewModel.searchBooks("Lord of the Rings") }) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Search 'LOTR'")
                            }
                        }
                    ) {
                        if (uiState.books.isEmpty()) {
                            Text("Search for books to see results here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(uiState.books) { book ->
                    ListItem(
                        headlineContent = { Text(book.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column {
                                if (book.authors.isNotEmpty()) { Text("By ${book.authors.joinToString(", ")}") }
                                book.firstPublishYear?.let { Text("Published: $it") }
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Book, contentDescription = null)
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    )
                    HorizontalDivider()
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

