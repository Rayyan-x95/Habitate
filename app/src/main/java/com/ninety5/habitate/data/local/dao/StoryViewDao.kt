package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.StoryViewEntity

@Dao
interface StoryViewDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(view: StoryViewEntity)

    @Query("SELECT COUNT(*) FROM story_views WHERE storyId = :storyId")
    suspend fun getViewCount(storyId: String): Int
}
