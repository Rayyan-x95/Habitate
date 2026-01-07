package com.ninety5.habitate.ui.screens.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.ui.screens.story.StoryViewModel

@Composable
fun StoryViewerScreen(
    userId: String,
    onClose: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val stories by viewModel.activeStories.collectAsState()
    val userStories = stories.filter { it.story.userId == userId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (userStories.isEmpty()) {
            Text(
                text = "No stories for this user",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Simple placeholder for actual story content (image/video)
            // In a real app, this would be a Pager
            val currentStory = userStories.first()
            Text(
                text = "Story: ${currentStory.story.id}\nMedia: ${currentStory.story.mediaUrl}",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}
