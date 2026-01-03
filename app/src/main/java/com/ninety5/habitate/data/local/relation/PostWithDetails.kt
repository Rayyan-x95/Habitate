package com.ninety5.habitate.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.WorkoutEntity

data class PostWithDetails(
    @Embedded val post: PostEntity,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: UserEntity?,
    @Relation(
        parentColumn = "workoutId",
        entityColumn = "id"
    )
    val workout: WorkoutEntity?
)
