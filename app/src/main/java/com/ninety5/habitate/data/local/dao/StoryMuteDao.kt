package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.StoryMuteEntity

@Dao
interface StoryMuteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mute: StoryMuteEntity)

    @Query("DELETE FROM story_mutes WHERE userId = :userId AND mutedUserId = :mutedUserId")
    suspend fun delete(userId: String, mutedUserId: String)
}
