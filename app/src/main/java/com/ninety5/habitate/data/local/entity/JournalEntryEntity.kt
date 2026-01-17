package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "journal_entries",
    indices = [
        Index("userId"),
        Index("date"),
        Index("mood"),
        Index("syncState")
    ]
)
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String?,
    val content: String,
    val mood: String?,
    val tags: List<String>,
    val mediaUrls: List<String>,
    val date: Long,
    val isPrivate: Boolean,
    val syncState: SyncState,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Instant = Instant.now()
)
