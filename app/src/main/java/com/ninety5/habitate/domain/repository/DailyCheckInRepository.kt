package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for daily check-in / summary operations.
 */
interface DailyCheckInRepository {
    fun observeTodaySummary(): Flow<DailySummary?>
    suspend fun saveCheckIn(mood: String?, notes: String?): AppResult<Unit>
}
