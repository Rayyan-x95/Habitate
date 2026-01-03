package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "tasks", indices = [Index("dueAt"), Index("status")])
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueAt: Instant?,
    val recurrenceRule: String?,      // iCal RRULE format
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus,           // OPEN, DONE, ARCHIVED
    val linkedEntityId: String? = null,
    val linkedEntityType: String? = null, // "workout", "habit", "post"
    val syncState: SyncState,         // PENDING, SYNCED, FAILED
    val updatedAt: Instant
)
