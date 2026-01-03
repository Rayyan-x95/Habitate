package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_mutes")
data class StoryMuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // The user who performed the mute
    val mutedUserId: String, // The user whose stories are muted
    val createdAt: Long = System.currentTimeMillis()
)
