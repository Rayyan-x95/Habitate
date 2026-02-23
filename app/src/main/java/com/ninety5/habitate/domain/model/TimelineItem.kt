package com.ninety5.habitate.domain.model

/**
 * Domain model for a timeline entry (domain equivalent of the Room DatabaseView).
 */
data class TimelineItem(
    val id: String,
    val type: String,
    val timestamp: Long,
    val title: String?,
    val subtitle: String?,
    val isArchived: Boolean
)
