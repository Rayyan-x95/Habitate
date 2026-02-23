package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for focus/Pomodoro session operations.
 */
interface FocusRepository {
    fun observeActiveSessions(): Flow<FocusSession?>
    fun observeSessionHistory(): Flow<List<FocusSession>>
    suspend fun startSession(durationSeconds: Long, soundTrack: String?): AppResult<FocusSession>
    suspend fun pauseSession(sessionId: String): AppResult<Unit>
    suspend fun resumeSession(sessionId: String): AppResult<Unit>
    suspend fun completeSession(sessionId: String, rating: Int?): AppResult<FocusSession>
    suspend fun cancelSession(sessionId: String): AppResult<Unit>
    suspend fun getTotalFocusMinutesToday(): AppResult<Long>
    suspend fun saveCompletedSession(durationSeconds: Long, soundTrack: String?): AppResult<FocusSession>
}
