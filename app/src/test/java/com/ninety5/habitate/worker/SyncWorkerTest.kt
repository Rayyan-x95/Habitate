package com.ninety5.habitate.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.ninety5.habitate.data.local.dao.CommentDao
import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var followDao: FollowDao
    private lateinit var likeDao: LikeDao
    private lateinit var commentDao: CommentDao
    private lateinit var postDao: PostDao
    private lateinit var taskDao: TaskDao
    private lateinit var workoutDao: WorkoutDao
    private lateinit var messageDao: MessageDao
    private lateinit var apiService: ApiService
    private lateinit var moshi: Moshi

    private lateinit var syncWorker: SyncWorker

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        every { workerParams.id } returns UUID.randomUUID()
        every { workerParams.inputData } returns androidx.work.Data.EMPTY
        every { workerParams.tags } returns emptySet()
        every { workerParams.runAttemptCount } returns 0
        
        syncQueueDao = mockk(relaxed = true)
        followDao = mockk(relaxed = true)
        likeDao = mockk(relaxed = true)
        commentDao = mockk(relaxed = true)
        postDao = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)
        workoutDao = mockk(relaxed = true)
        messageDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        moshi = mockk(relaxed = true)

        syncWorker = SyncWorker(
            context,
            workerParams,
            syncQueueDao,
            followDao,
            likeDao,
            commentDao,
            postDao,
            taskDao,
            workoutDao,
            messageDao,
            apiService,
            moshi
        )
    }

    @Test
    fun `doWork should return success when no pending operations`() = runBlocking {
        coEvery { syncQueueDao.getPendingOperations() } returns emptyList()

        val result = syncWorker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork should sync task creation successfully`() = runBlocking {
        val op = SyncOperationEntity(
            id = 1,
            entityType = "task",
            entityId = "task_123",
            operation = "CREATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )

        coEvery { syncQueueDao.getPendingOperations() } returns listOf(op)
        coEvery { apiService.create(any(), any()) } returns mockk()

        val result = syncWorker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS) }
        coVerify { apiService.create("tasks", any()) }
        coVerify { syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED) }
    }

    @Test
    fun `doWork should sync workout creation successfully`() = runBlocking {
        val op = SyncOperationEntity(
            id = 2,
            entityType = "workout",
            entityId = "workout_123",
            operation = "CREATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )

        coEvery { syncQueueDao.getPendingOperations() } returns listOf(op)
        coEvery { apiService.create(any(), any()) } returns mockk()

        val result = syncWorker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS) }
        coVerify { apiService.create("workouts", any()) }
        coVerify { syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED) }
    }

    @Test
    fun `doWork should handle failure and retry`() = runBlocking {
        val op = SyncOperationEntity(
            id = 3,
            entityType = "task",
            entityId = "task_fail",
            operation = "CREATE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null,
            retryCount = 0
        )

        coEvery { syncQueueDao.getPendingOperations() } returns listOf(op)
        coEvery { apiService.create(any(), any()) } throws RuntimeException("Network error")

        val result = syncWorker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS) }
        coVerify { syncQueueDao.updateRetry(op.id, 1, SyncStatus.PENDING) }
    }
}
