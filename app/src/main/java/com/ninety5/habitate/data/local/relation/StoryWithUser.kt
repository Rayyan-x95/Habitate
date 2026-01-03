package com.ninety5.habitate.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.UserEntity

data class StoryWithUser(
    @Embedded val story: StoryEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity?
)
