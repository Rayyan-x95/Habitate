package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.JournalDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val syncQueueDao: SyncQueueDao,
    private val authRepository: AuthRepository,
    private val moshi: Moshi
) {

    fun getAllEntries(): Flow<List<JournalEntryEntity>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())
        return journalDao.getAllEntries(userId)
    }

    fun getEntryById(id: String): Flow<JournalEntryEntity?> {
        return journalDao.getEntryById(id)
    }

    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntryEntity>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return journalDao.getEntriesForDateRange(userId, startOfDay, endOfDay)
    }

    fun getEntriesForMonth(year: Int, month: Int): Flow<List<JournalEntryEntity>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = startOfMonth + (32L * 24 * 60 * 60 * 1000) // Rough end of month
        return journalDao.getEntriesForDateRange(userId, startOfMonth, endOfMonth)
    }

    suspend fun createEntry(
        title: String?,
        content: String,
        mood: String?,
        tags: List<String> = emptyList(),
        mediaUrls: List<String> = emptyList(),
        isPrivate: Boolean = true
    ): JournalEntryEntity {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
        
        val entryId = UUID.randomUUID().toString()
        val now = Instant.now()
        
        val entry = JournalEntryEntity(
            id = entryId,
            userId = userId,
            title = title,
            content = content,
            mood = mood,
            tags = tags,
            mediaUrls = mediaUrls,
            date = now.toEpochMilli(),
            isPrivate = isPrivate,
            syncState = SyncState.PENDING,
            updatedAt = now.toEpochMilli(),
            createdAt = now
        )
        
        journalDao.upsert(entry)
        
        // Queue sync operation
        val payload = moshi.adapter(JournalEntryEntity::class.java).toJson(entry)
        val syncOp = SyncOperationEntity(
            entityType = "journal",
            entityId = entryId,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = now,
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
        
        return entry
    }

    suspend fun updateEntry(entry: JournalEntryEntity) {
        val now = Instant.now()
        val updatedEntry = entry.copy(
            syncState = SyncState.PENDING,
            updatedAt = now.toEpochMilli()
        )
        
        journalDao.upsert(updatedEntry)
        
        // Queue sync operation
        val payload = moshi.adapter(JournalEntryEntity::class.java).toJson(updatedEntry)
        val syncOp = SyncOperationEntity(
            entityType = "journal",
            entityId = entry.id,
            operation = "UPDATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = now,
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun deleteEntry(entryId: String) {
        journalDao.deleteById(entryId)
        
        // Queue sync operation
        val syncOp = SyncOperationEntity(
            entityType = "journal",
            entityId = entryId,
            operation = "DELETE",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    suspend fun exportToJson(): String {
        val userId = authRepository.getCurrentUserId() ?: return "[]"
        val entries = journalDao.getAllEntriesSync(userId)
        return moshi.adapter<List<JournalEntryEntity>>(
            com.squareup.moshi.Types.newParameterizedType(
                List::class.java,
                JournalEntryEntity::class.java
            )
        ).toJson(entries)
    }

    fun searchEntries(query: String): Flow<List<JournalEntryEntity>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())
        return journalDao.searchEntries(userId, "%$query%")
    }

    fun getEntriesByMood(mood: String): Flow<List<JournalEntryEntity>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())
        return journalDao.getEntriesByMood(userId, mood)
    }
}
