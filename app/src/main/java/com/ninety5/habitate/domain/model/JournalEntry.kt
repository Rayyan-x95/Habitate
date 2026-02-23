package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a journal entry.
 */
data class JournalEntry(
    val id: String,
    val userId: String,
    val title: String?,
    val content: String,
    val mood: JournalMood?,
    val tags: List<String>,
    val mediaUrls: List<String>,
    val isPrivate: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class JournalMood {
    AMAZING, HAPPY, NEUTRAL, SAD, TERRIBLE
}
