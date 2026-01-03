package com.ninety5.habitate.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ninety5.habitate.data.local.dao.TimelineDao
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepository @Inject constructor(
    private val timelineDao: TimelineDao,
    private val postDao: PostDao,
    private val workoutDao: WorkoutDao,
    private val taskDao: TaskDao
) {
    fun getTimeline(typeFilter: String? = null, searchQuery: String? = null): Flow<PagingData<TimelineItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                when {
                    !searchQuery.isNullOrBlank() -> timelineDao.searchTimelinePaged(searchQuery)
                    typeFilter == null || typeFilter == "ALL" -> timelineDao.getTimelinePaged()
                    else -> timelineDao.getTimelinePagedByType(typeFilter)
                }
            }
        ).flow
    }

    fun getTimelineByType(type: String): Flow<List<TimelineItem>> {
        return timelineDao.getTimelineByType(type)
    }

    fun getAllTimelineItems(): Flow<List<TimelineItem>> {
        return timelineDao.getTimeline()
    }

    fun getArchivedTimeline(): Flow<PagingData<TimelineItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { timelineDao.getTimelinePagedByArchiveStatus(true) }
        ).flow
    }

    suspend fun restoreItem(id: String, type: String) {
        when (type) {
            "post" -> postDao.restorePost(id)
            "workout" -> workoutDao.restoreWorkout(id)
            "task" -> taskDao.updateStatus(id, TaskStatus.DONE) // Restore to DONE (or OPEN?) - DONE seems safer
        }
    }
}
