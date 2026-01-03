package com.ninety5.habitate.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.UserEntity

/**
 * Data class combining comment with author information.
 * Used with Room's @Transaction annotation for efficient JOIN queries.
 */
data class CommentWithUser(
    @Embedded val comment: CommentEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)
