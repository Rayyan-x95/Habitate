package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Insight
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for AI-powered insights.
 */
interface InsightRepository {
    fun observeActiveInsights(): Flow<List<Insight>>
    suspend fun generateInsights(): AppResult<List<Insight>>
    suspend fun dismissInsight(insightId: String): AppResult<Unit>
    suspend fun refreshInsights(): AppResult<Unit>
}
