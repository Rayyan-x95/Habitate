package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_categories")
data class TaskCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String
)
