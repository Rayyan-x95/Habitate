package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.local.dao.HabitatDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.HabitatMembershipEntity
import com.ninety5.habitate.data.local.entity.HabitatPrivacy
import com.ninety5.habitate.data.local.entity.HabitatRole
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import com.squareup.moshi.Moshi
import com.ninety5.habitate.data.remote.ApiService

/**
 * Repository for managing habitats (groups/communities).
 * Handles CRUD operations and syncing with remote API.
 */
@Singleton
class HabitatRepository @Inject constructor(
    private val habitatDao: HabitatDao,
    private val syncQueueDao: SyncQueueDao,
    private val securePreferences: SecurePreferences,
    private val moshi: Moshi,
    private val apiService: ApiService
) {

    /**
     * Get all habitats.
     */
    fun getAllHabitats(): Flow<List<HabitatEntity>> {
        return habitatDao.getAllHabitats()
    }

    /**
     * Get habitats the user has joined.
     */
    fun getJoinedHabitats(): Flow<List<HabitatEntity>> {
        return habitatDao.getJoinedHabitats(securePreferences.userId ?: "")
    }

    /**
     * Get suggested habitats to discover.
     */
    fun getDiscoverHabitats(limit: Int = 20): Flow<List<HabitatEntity>> {
        return habitatDao.getDiscoverHabitats(securePreferences.userId ?: "")
    }

    /**
     * Get a single habitat by ID.
     */
    fun getHabitatById(habitatId: String): Flow<HabitatEntity?> {
        return habitatDao.getHabitatById(habitatId)
    }

    /**
     * Search habitats by name or tags.
     */
    fun searchHabitats(query: String): Flow<List<HabitatEntity>> {
        return habitatDao.searchHabitats(query)
    }

    /**
     * Create a new habitat.
     */
    suspend fun createHabitat(habitat: HabitatEntity, creatorId: String) {
        habitatDao.upsert(habitat.copy(
            syncState = SyncState.PENDING
        ))

        // Add creator as owner
        val membership = HabitatMembershipEntity(
            id = UUID.randomUUID().toString(),
            habitatId = habitat.id,
            userId = creatorId,
            role = HabitatRole.OWNER,
            syncState = SyncState.PENDING
        )
        habitatDao.upsertMembership(membership)
        
        val payload = moshi.adapter(HabitatEntity::class.java).toJson(habitat)
        val syncOp = SyncOperationEntity(
            entityType = "habitat",
            entityId = habitat.id,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Update an existing habitat.
     */
    suspend fun updateHabitat(habitat: HabitatEntity) {
        habitatDao.upsert(habitat.copy(
            syncState = SyncState.PENDING,
            updatedAt = System.currentTimeMillis()
        ))
        
        val payload = moshi.adapter(HabitatEntity::class.java).toJson(habitat)
        val syncOp = SyncOperationEntity(
            entityType = "habitat",
            entityId = habitat.id,
            operation = "UPDATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Join a habitat.
     */
    suspend fun joinHabitat(habitatId: String, userId: String) {
        habitatDao.updateMemberCount(habitatId, 1)

        val membership = HabitatMembershipEntity(
            id = UUID.randomUUID().toString(),
            habitatId = habitatId,
            userId = userId,
            role = HabitatRole.MEMBER,
            syncState = SyncState.PENDING
        )
        habitatDao.upsertMembership(membership)
        
        val payload = "{\"userId\": \"$userId\"}"
        val syncOp = SyncOperationEntity(
            entityType = "habitat_join",
            entityId = habitatId,
            operation = "CREATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Leave a habitat.
     */
    suspend fun leaveHabitat(habitatId: String, userId: String) {
        habitatDao.updateMemberCount(habitatId, -1)
        habitatDao.deleteMembership(habitatId, userId)
        
        val payload = "{\"userId\": \"$userId\"}"
        val syncOp = SyncOperationEntity(
            entityType = "habitat_leave",
            entityId = habitatId,
            operation = "DELETE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Delete a habitat (owner only).
     */
    suspend fun deleteHabitat(habitatId: String) {
        habitatDao.deleteById(habitatId)
        
        val syncOp = SyncOperationEntity(
            entityType = "habitat",
            entityId = habitatId,
            operation = "DELETE",
            payload = "",
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        )
        syncQueueDao.insert(syncOp)
    }

    /**
     * Refresh habitats from server.
     */
    suspend fun refreshHabitats(): Result<Unit> {
        return try {
            val habitatDtos = apiService.getHabitats()
            val habitats = habitatDtos.map { dto ->
                HabitatEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    coverImageUrl = dto.avatarUrl,
                    memberCount = 0, // Backend doesn't send this yet
                    privacy = try {
                        HabitatPrivacy.valueOf(dto.privacyLevel.uppercase())
                    } catch (e: Exception) {
                        HabitatPrivacy.PUBLIC
                    },
                    syncState = SyncState.SYNCED,
                    updatedAt = dto.updatedAt.toEpochMilli()
                )
            }
            habitatDao.upsertAll(habitats)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
