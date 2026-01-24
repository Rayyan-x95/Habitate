package com.ninety5.habitate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.ui.theme.MutedLilac
import com.ninety5.habitate.ui.theme.SageGreen
import com.ninety5.habitate.ui.theme.SoftIndigo
import com.ninety5.habitate.ui.theme.SoftRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reusable timeline item card component used by TimelineScreen and ArchiveScreen.
 * Displays a visual timeline with item type icons and content.
 */
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
                    .background(getTimelineTypeColor(item.type))
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
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
                        imageVector = getTimelineTypeIcon(item.type),
                        contentDescription = "${item.type} item",
                        tint = getTimelineTypeColor(item.type),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimelineDate(item.timestamp),
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

/**
 * Returns the color associated with a timeline item type.
 * Uses semantic colors from the theme palette.
 */
fun getTimelineTypeColor(type: String): Color {
    return when (type) {
        "post" -> SoftIndigo           // From theme
        "workout" -> SoftRed           // From theme
        "story" -> Color(0xFFF59E0B)   // Amber (keep for now - add to theme later)
        "task" -> SageGreen            // From theme
        "insight" -> MutedLilac        // From theme
        "journal" -> Color(0xFFEC4899) // Pink (keep for now - add to theme later)
        else -> Color(0xFF9CA3AF)      // Neutral gray
    }
}

/**
 * Returns the icon associated with a timeline item type.
 */
fun getTimelineTypeIcon(type: String): ImageVector {
    return when (type) {
        "post" -> Icons.Default.Edit
        "workout" -> Icons.Default.FitnessCenter
        "story" -> Icons.Default.History
        "task" -> Icons.Default.CheckCircle
        "insight" -> Icons.Default.Lightbulb
        "journal" -> Icons.Default.Edit
        else -> Icons.Default.History
    }
}

/**
 * Formats a timestamp for display in the timeline.
 */
fun formatTimelineDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
}
