package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Challenge
import com.ninety5.habitate.domain.model.ChallengeProgress
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for challenge operations.
 */
interface ChallengeRepository {
    fun observeActiveChallenges(): Flow<List<Challenge>>
    suspend fun getChallenge(challengeId: String): AppResult<Challenge>
    suspend fun joinChallenge(challengeId: String): AppResult<Unit>
    suspend fun leaveChallenge(challengeId: String): AppResult<Unit>
    suspend fun getLeaderboard(challengeId: String): AppResult<List<ChallengeProgress>>
    suspend fun updateProgress(challengeId: String, value: Double): AppResult<Unit>
}
