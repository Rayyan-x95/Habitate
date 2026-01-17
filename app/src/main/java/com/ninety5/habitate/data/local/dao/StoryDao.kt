package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.StoryViewEntity
import com.ninety5.habitate.data.local.relation.StoryWithUser
import kotlinx.coroutines.flow.Flow

import com.ninety5.habitate.data.local.entity.SyncState

@Dao
interface StoryDao {
    @androidx.room.Transaction
    @Query("""
        SELECT * FROM stories 
        WHERE expiresAt > :now 
        AND userId NOT IN (SELECT mutedUserId FROM story_mutes WHERE userId = :currentUserId)
        ORDER BY createdAt DESC
    """)
    fun getActiveStories(currentUserId: String, now: Long = System.currentTimeMillis()): Flow<List<StoryWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(story: StoryEntity)

    @Query("UPDATE stories SET syncState = :status WHERE id = :id")
    suspend fun updateSyncState(id: String, status: SyncState)

    @Query("DELETE FROM stories WHERE expiresAt < :now")
    suspend fun deleteExpiredStories(now: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStoryView(view: StoryViewEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM story_views WHERE storyId = :storyId AND viewerId = :viewerId)")
    suspend fun hasViewedStory(storyId: String, viewerId: String): Boolean
}
