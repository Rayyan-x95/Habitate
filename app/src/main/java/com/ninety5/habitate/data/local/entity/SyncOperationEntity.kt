package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "sync_queue")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,       // "task", "workout", "post"
    val entityId: String,
    val operation: String,        // "CREATE", "UPDATE", "DELETE"
    val payload: String,          // JSON payload
    val status: SyncStatus,       // PENDING, IN_PROGRESS, FAILED, COMPLETED
    val retryCount: Int = 0,
    val createdAt: Instant,
    val lastAttemptAt: Instant?
)
