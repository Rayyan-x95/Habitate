package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a task.
 */
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val status: TaskStatus,
    val dueAt: Instant?,
    val recurrenceRule: String?,
    val categoryId: String?,
    val linkedEntityId: String?,
    val linkedEntityType: String?,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
