package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.InsightDao
import com.ninety5.habitate.data.local.entity.InsightEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightRepository @Inject constructor(
    private val insightDao: InsightDao
) {
    fun getActiveInsights(): Flow<List<InsightEntity>> {
        return insightDao.getActiveInsights()
    }

    suspend fun dismissInsight(id: String) {
        insightDao.dismiss(id)
    }

    suspend fun markAsActioned(id: String) {
        // For now, actioned insights are treated as dismissed
        insightDao.dismiss(id)
    }

    suspend fun addInsight(insight: InsightEntity) {
        insightDao.insert(insight)
    }
}
