package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Post entity representing a social post in the local database.
 */
@Entity(
    tableName = "posts",
    indices = [
        Index("authorId"),
        Index("habitatId"),
        Index("createdAt"),
        Index("syncState")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostEntity(
    @PrimaryKey
    val id: String,
    val authorId: String,
    val contentText: String?,
    val mediaUrls: List<String> = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val habitatId: String?,
    val workoutId: String?,
    val linkedEntityId: String? = null,
    val linkedEntityType: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val reactionType: String? = null,
    val isArchived: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
