package com.ninety5.habitate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Input field for creating new comments.
 * Includes character counter and send button.
 */
@Composable
fun CommentInput(
    onSendComment: (String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    maxLength: Int = 500
) {
    var commentText by remember { mutableStateOf("") }
    val remainingChars = maxLength - commentText.length
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Text Input
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { 
                        if (it.length <= maxLength) {
                            commentText = it
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading,
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Send Button
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onSendComment(commentText.trim())
                            commentText = ""
                        }
                    },
                    enabled = !isLoading && commentText.isNotBlank(),
                    modifier = Modifier.size(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send comment",
                            tint = if (commentText.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                }
            }
            
            // Character Counter
            if (remainingChars < 100) {
                Text(
                    text = "$remainingChars characters remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingChars < 20) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
