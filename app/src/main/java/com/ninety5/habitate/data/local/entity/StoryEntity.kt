package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import com.ninety5.habitate.data.local.entity.SyncState

@Entity(
    tableName = "stories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isSaved"])
    ]
)
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String,
    val caption: String? = null,
    val visibility: Visibility = Visibility.PUBLIC,
    val createdAt: Long,
    val expiresAt: Long,
    val isSaved: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED
)
