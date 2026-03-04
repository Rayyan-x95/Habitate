package com.ninety5.habitate.ui.screens.habitats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ninety5.habitate.ui.components.PostItem
import com.ninety5.habitate.ui.theme.HabitateTheme
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitatDetailScreen(
    habitatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToChallenge: (String) -> Unit,
    viewModel: HabitatDetailViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(habitatId) {
        viewModel.loadHabitat(habitatId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val habitat = uiState.habitat
    val activeChallenge = uiState.activeChallenge
    val context = LocalContext.current

    Scaffold(
        containerColor = HabitateTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = habitat?.name ?: "Habitat",
                        color = HabitateTheme.colors.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = "Back",
                            tint = HabitateTheme.colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HabitateTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && habitat == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    item {
                        if (habitat != null) {
                            Column {
                                if (habitat.coverUrl != null) {
                                    AsyncImage(
                                        model = habitat.coverUrl,
                                        contentDescription = "Cover Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                Text(
                                    text = habitat.description ?: "",
                                    style = HabitateTheme.typography.bodyMedium,
                                    color = HabitateTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${habitat.memberCount} members",
                                    style = HabitateTheme.typography.labelMedium,
                                    color = HabitateTheme.colors.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                if (activeChallenge != null) {
                                    Card(
                                        onClick = { onNavigateToChallenge(activeChallenge.id) },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1A2C24)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.EmojiEvents,
                                                contentDescription = null,
                                                tint = HabitateTheme.colors.primary
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    "Active Challenge",
                                                    style = HabitateTheme.typography.labelMedium,
                                                    color = HabitateTheme.colors.onBackground.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    activeChallenge.title,
                                                    style = HabitateTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HabitateTheme.colors.onBackground
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                Text(
                                    text = "Recent Posts",
                                    style = HabitateTheme.typography.titleMedium,
                                    color = HabitateTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // Posts
                    items(uiState.posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            onLikeClick = { viewModel.toggleLike(post.id) },
                            onReactionClick = { reaction -> viewModel.toggleLike(post.id, reaction) },
                            onCommentClick = { onNavigateToPost(post.id) },
                            onShareClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this post by ${post.authorName}: ${post.contentText}")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            onUserClick = { onNavigateToUser(post.authorId) }
                        )
                    }
                }
            }
        }
    }
}
