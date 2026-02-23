package com.ninety5.habitate.domain.repository

import androidx.paging.PagingData
import com.ninety5.habitate.domain.model.TimelineItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for timeline / archive operations.
 */
interface TimelineRepository {
    fun getTimeline(typeFilter: String? = null, searchQuery: String? = null): Flow<PagingData<TimelineItem>>
    fun getArchivedTimeline(): Flow<PagingData<TimelineItem>>
    suspend fun restoreItem(id: String, type: String)
}
