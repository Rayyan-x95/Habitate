package com.ninety5.habitate.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.ninety5.habitate.data.local.view.TimelineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_view ORDER BY timestamp DESC")
    fun getTimeline(): Flow<List<TimelineItem>>

    @Query("SELECT * FROM timeline_view ORDER BY timestamp DESC")
    fun getTimelinePaged(): PagingSource<Int, TimelineItem>

    @Query("SELECT * FROM timeline_view WHERE type = :type ORDER BY timestamp DESC")
    fun getTimelinePagedByType(type: String): PagingSource<Int, TimelineItem>

    @Query("SELECT * FROM timeline_view WHERE type = :type ORDER BY timestamp DESC")
    fun getTimelineByType(type: String): Flow<List<TimelineItem>>
    
    @Query("SELECT * FROM timeline_view WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getTimelineByDateRange(start: Long, end: Long): Flow<List<TimelineItem>>

    @Query("SELECT * FROM timeline_view WHERE isArchived = :isArchived ORDER BY timestamp DESC")
    fun getTimelinePagedByArchiveStatus(isArchived: Boolean): PagingSource<Int, TimelineItem>

    @Query("SELECT * FROM timeline_view WHERE title LIKE '%' || :query || '%' OR subtitle LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTimelinePaged(query: String): PagingSource<Int, TimelineItem>
}
