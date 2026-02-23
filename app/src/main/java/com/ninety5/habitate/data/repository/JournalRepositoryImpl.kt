package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.JournalDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.domain.model.JournalMood
import com.ninety5.habitate.domain.repository.JournalRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Concrete implementation of [JournalRepository].
 *
 * Handles:
 * - CRUD operations for journal entries
 * - Mood-based filtering
 * - Date-based querying
 * - Offline-first with sync queue
 * - JSON export
 */
@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val syncQueueDao: SyncQueueDao,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi
) : JournalRepository {

    // ══════════════════════════════════════════════════════════════════════
    // DOMAIN INTERFACE METHODS
    // ══════════════════════════════════════════════════════════════════════

    override fun observeAllEntries(): Flow<List<JournalEntry>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return journalDao.getAllEntries(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEntry(entryId: String): AppResult<JournalEntry> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val entity = journalDao.getEntryByIdAndUserIdSync(entryId, userId)
                ?: return AppResult.Error(AppError.NotFound("Journal entry not found"))
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get journal entry: $entryId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to get journal entry"))
        }
    }

    override suspend fun createEntry(entry: JournalEntry): AppResult<JournalEntry> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val now = Instant.now()
            val created = entry.copy(
                id = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id,
                userId = userId,
                createdAt = now,
                updatedAt = now
            )
            val entity = created.toEntity(SyncState.PENDING)

            journalDao.upsert(entity)
            queueSync("journal", entity.id, "CREATE",
                moshi.adapter(JournalEntryEntity::class.java).toJson(entity))

            Timber.d("Created journal entry: ${entity.id}")
            AppResult.Success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to create journal entry")
            AppResult.Error(AppError.Database(e.message ?: "Failed to create journal entry"))
        }
    }

    override suspend fun updateEntry(entry: JournalEntry): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val existing = journalDao.getEntryByIdAndUserIdSync(entry.id, userId)
                ?: return AppResult.Error(AppError.NotFound("Journal entry not found"))

            val now = Instant.now()
            val updated = entry.copy(
                userId = existing.userId, // Preserve original owner
                updatedAt = now
            ).toEntity(SyncState.PENDING)

            journalDao.upsert(updated)
            queueSync("journal", entry.id, "UPDATE",
                moshi.adapter(JournalEntryEntity::class.java).toJson(updated))

            Timber.d("Updated journal entry: ${entry.id}")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update journal entry: ${entry.id}")
            AppResult.Error(AppError.Database(e.message ?: "Failed to update journal entry"))
        }
    }

    override suspend fun deleteEntry(entryId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val deletedCount = journalDao.deleteByIdAndUserId(entryId, userId)
            if (deletedCount == 0) {
                return AppResult.Error(AppError.NotFound("Journal entry not found or unauthorized"))
            }

            queueSync("journal", entryId, "DELETE", "{}")

            Timber.d("Deleted journal entry: $entryId")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete journal entry: $entryId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to delete journal entry"))
        }
    }

    override suspend fun getEntriesByMood(mood: JournalMood): AppResult<List<JournalEntry>> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val entities = journalDao.getEntriesByMood(userId, mood.name)
                .firstOrNull() ?: emptyList()
            AppResult.Success(entities.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get entries by mood: $mood")
            AppResult.Error(AppError.Database(e.message ?: "Failed to get entries by mood"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEGACY VIEWMODEL-FACING METHODS
    // These support existing ViewModels that haven't migrated to domain interface.
    // ══════════════════════════════════════════════════════════════════════

    override fun observeEntriesForDate(date: LocalDate): Flow<List<JournalEntry>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return journalDao.getEntriesForDateRange(userId, startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return journalDao.searchEntries(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun exportToJson(): AppResult<String> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val entries = journalDao.getAllEntriesSync(userId)
            val json = moshi.adapter<List<JournalEntryEntity>>(
                Types.newParameterizedType(List::class.java, JournalEntryEntity::class.java)
            ).toJson(entries)
            AppResult.Success(json)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to export journal")
            AppResult.Error(AppError.Database(e.message ?: "Export failed"))
        }
    }

    fun getAllEntries(): Flow<List<JournalEntryEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return journalDao.getAllEntries(userId)
    }

    fun getEntryById(id: String): Flow<JournalEntryEntity?> {
        val userId = securePreferences.userId ?: return flowOf(null)
        return journalDao.getEntryByIdAndUserId(id, userId)
    }

    fun getEntriesForDate(date: LocalDate): Flow<List<JournalEntryEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return journalDao.getEntriesForDateRange(userId, startOfDay, endOfDay)
    }

    fun getEntriesForMonth(year: Int, month: Int): Flow<List<JournalEntryEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = LocalDate.of(year, month, 1)
            .with(TemporalAdjusters.lastDayOfMonth())
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
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
        val userId = securePreferences.userId
            ?: error("User not logged in")

        val entryId = UUID.randomUUID().toString()
        val now = Instant.now()

        val entity = JournalEntryEntity(
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

        journalDao.upsert(entity)
        queueSync("journal", entryId, "CREATE",
            moshi.adapter(JournalEntryEntity::class.java).toJson(entity))

        return entity
    }

    suspend fun updateEntry(entry: JournalEntryEntity) {
        val userId = securePreferences.userId
            ?: error("User not logged in")

        val existing = journalDao.getEntryByIdAndUserIdSync(entry.id, userId)
            ?: error("Entry not found or unauthorized")

        val now = Instant.now()
        val updated = entry.copy(
            userId = existing.userId,
            syncState = SyncState.PENDING,
            updatedAt = now.toEpochMilli()
        )

        journalDao.upsert(updated)
        queueSync("journal", entry.id, "UPDATE",
            moshi.adapter(JournalEntryEntity::class.java).toJson(updated))
    }

    fun searchEntriesLegacy(query: String): Flow<List<JournalEntryEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return journalDao.searchEntries(userId, query)
    }

    fun getEntriesByMood(mood: String): Flow<List<JournalEntryEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return journalDao.getEntriesByMood(userId, mood)
    }

    suspend fun exportToJsonLegacy(): String {
        val userId = securePreferences.userId ?: return "[]"
        val entries = journalDao.getAllEntriesSync(userId)
        return moshi.adapter<List<JournalEntryEntity>>(
            Types.newParameterizedType(List::class.java, JournalEntryEntity::class.java)
        ).toJson(entries)
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun queueSync(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String
    ) {
        syncQueueDao.insert(
            SyncOperationEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = payload,
                status = SyncStatus.PENDING,
                createdAt = Instant.now(),
                lastAttemptAt = null
            )
        )
    }
}
