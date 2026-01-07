package com.ninety5.habitate.ui.screens.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.ui.screens.timeline.TimelineItemCard
import com.ninety5.habitate.ui.screens.archive.ArchiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val archivedItems = viewModel.archivedItems.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archive") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                count = archivedItems.itemCount,
                key = archivedItems.itemKey { it.id },
                contentType = archivedItems.itemContentType { it.type }
            ) { index ->
                val item = archivedItems[index]
                if (item != null) {
                    ArchivedItemCard(
                        item = item,
                        onRestore = { viewModel.restoreItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedItemCard(
    item: TimelineItem,
    onRestore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TimelineItemCard(item)
        Button(
            onClick = onRestore,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp),
            colors = ButtonDefaults.textButtonColors()
        ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Text("Restore", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
