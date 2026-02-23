package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for story operations.
 */
interface StoryRepository {
    fun observeActiveStories(): Flow<List<Story>>
    suspend fun createStory(mediaUrl: String, caption: String?, visibility: String): AppResult<Story>
    suspend fun deleteStory(storyId: String): AppResult<Unit>
    suspend fun markAsViewed(storyId: String): AppResult<Unit>
    suspend fun saveStory(storyId: String): AppResult<Unit>
    suspend fun muteUserStories(userId: String): AppResult<Unit>
    suspend fun unmuteUserStories(userId: String): AppResult<Unit>
    suspend fun refreshStories(): AppResult<Unit>
}
