package com.ninety5.habitate.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ninety5.habitate.data.local.dao.TimelineDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.entity.TaskStatus
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.model.TimelineItem
import com.ninety5.habitate.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepositoryImpl @Inject constructor(
    private val timelineDao: TimelineDao,
    private val postDao: PostDao,
    private val workoutDao: WorkoutDao,
    private val taskDao: TaskDao
) : TimelineRepository {
    override fun getTimeline(typeFilter: String?, searchQuery: String?): Flow<PagingData<TimelineItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                when {
                    !searchQuery.isNullOrBlank() -> timelineDao.searchTimelinePaged(searchQuery)
                    typeFilter == null || typeFilter == "ALL" -> timelineDao.getTimelinePaged()
                    else -> timelineDao.getTimelinePagedByType(typeFilter)
                }
            }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    fun getTimelineByType(type: String): Flow<List<TimelineItem>> {
        return timelineDao.getTimelineByType(type).map { list -> list.map { it.toDomain() } }
    }

    fun getAllTimelineItems(): Flow<List<TimelineItem>> {
        return timelineDao.getTimeline().map { list -> list.map { it.toDomain() } }
    }

    override fun getArchivedTimeline(): Flow<PagingData<TimelineItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { timelineDao.getTimelinePagedByArchiveStatus(true) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun restoreItem(id: String, type: String) {
        when (type) {
            "post" -> postDao.restorePost(id, System.currentTimeMillis())
            "workout" -> workoutDao.restoreWorkout(id, java.time.Instant.now())
            "task" -> taskDao.updateStatus(id, TaskStatus.OPEN, System.currentTimeMillis()) // Restore to active state
            else -> throw IllegalArgumentException("Unknown timeline item type: $type")
        }
    }
}
