package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.local.dao.HabitatDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.HabitatMembershipEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.HabitatPrivacy as EntityHabitatPrivacy
import com.ninety5.habitate.data.local.entity.HabitatRole as EntityHabitatRole
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toDomainPrivacy
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.mapper.toEntityPrivacy
import com.ninety5.habitate.domain.mapper.toEntityRole
import com.ninety5.habitate.domain.model.Habitat
import com.ninety5.habitate.domain.model.HabitatMembership
import com.ninety5.habitate.domain.model.HabitatRole
import com.ninety5.habitate.domain.repository.HabitatRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Concrete implementation of [HabitatRepository].
 *
 * Handles:
 * - Habitat CRUD operations
 * - Membership management (join/leave/role updates)
 * - Habitat discovery and search
 * - Offline-first with sync queue
 */
@Singleton
class HabitatRepositoryImpl @Inject constructor(
    private val habitatDao: HabitatDao,
    private val syncQueueDao: SyncQueueDao,
    private val securePreferences: SecurePreferences,
    private val apiService: ApiService,
    private val moshi: Moshi
) : HabitatRepository {

    // ══════════════════════════════════════════════════════════════════════
    // DOMAIN INTERFACE METHODS
    // ══════════════════════════════════════════════════════════════════════

    override fun observeMyHabitats(): Flow<List<Habitat>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitatDao.getJoinedHabitats(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeHabitat(habitatId: String): Flow<Habitat?> {
        return habitatDao.getHabitatById(habitatId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun discoverHabitats(): Flow<List<Habitat>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitatDao.getDiscoverHabitats(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHabitat(habitatId: String): AppResult<Habitat> {
        return try {
            val entity = habitatDao.getHabitatById(habitatId).firstOrNull()
                ?: return AppResult.Error(AppError.NotFound("Habitat not found"))
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get habitat: $habitatId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to get habitat"))
        }
    }

    override suspend fun createHabitat(
        name: String,
        description: String?,
        privacy: String
    ): AppResult<Habitat> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            val habitatId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val entityPrivacy = try {
                EntityHabitatPrivacy.valueOf(privacy.uppercase())
            } catch (_: Exception) {
                EntityHabitatPrivacy.PUBLIC
            }

            val entity = HabitatEntity(
                id = habitatId,
                name = name,
                description = description,
                coverImageUrl = null,
                memberCount = 1,
                privacy = entityPrivacy,
                syncState = SyncState.PENDING,
                updatedAt = now
            )
            habitatDao.upsert(entity)

            // Add creator as owner
            val membership = HabitatMembershipEntity(
                id = UUID.randomUUID().toString(),
                habitatId = habitatId,
                userId = userId,
                role = EntityHabitatRole.OWNER,
                syncState = SyncState.PENDING
            )
            habitatDao.upsertMembership(membership)

            queueSync("habitat", habitatId, "CREATE",
                moshi.adapter(HabitatEntity::class.java).toJson(entity))

            Timber.d("Created habitat: $name")
            AppResult.Success(entity.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to create habitat")
            AppResult.Error(AppError.Database(e.message ?: "Failed to create habitat"))
        }
    }

    override suspend fun updateHabitat(habitat: Habitat): AppResult<Unit> {
        return try {
            val entity = habitat.toEntity(SyncState.PENDING)
                .copy(updatedAt = System.currentTimeMillis())
            habitatDao.upsert(entity)
            queueSync("habitat", habitat.id, "UPDATE",
                moshi.adapter(HabitatEntity::class.java).toJson(entity))

            Timber.d("Updated habitat: ${habitat.id}")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update habitat: ${habitat.id}")
            AppResult.Error(AppError.Database(e.message ?: "Failed to update habitat"))
        }
    }

    override suspend fun deleteHabitat(habitatId: String): AppResult<Unit> {
        return try {
            habitatDao.deleteById(habitatId)
            queueSync("habitat", habitatId, "DELETE", "{}")

            Timber.d("Deleted habitat: $habitatId")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete habitat: $habitatId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to delete habitat"))
        }
    }

    override suspend fun joinHabitat(habitatId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Check if already a member to prevent duplicate joins
            val existingMembers = habitatDao.getMemberships(habitatId)
            if (existingMembers.any { it.userId == userId }) {
                return AppResult.Error(AppError.Validation("Already a member of this habitat"))
            }

            habitatDao.updateMemberCount(habitatId, 1)

            val membership = HabitatMembershipEntity(
                id = UUID.randomUUID().toString(),
                habitatId = habitatId,
                userId = userId,
                role = EntityHabitatRole.MEMBER,
                syncState = SyncState.PENDING
            )
            habitatDao.upsertMembership(membership)

            queueSync("habitat_join", habitatId, "CREATE",
                """{"userId":"$userId","habitatId":"$habitatId"}""")

            Timber.d("Joined habitat: $habitatId")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to join habitat: $habitatId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to join habitat"))
        }
    }

    override suspend fun leaveHabitat(habitatId: String): AppResult<Unit> {
        val userId = securePreferences.userId
            ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))

        return try {
            // Verify membership exists before leaving
            val existingMembers = habitatDao.getMemberships(habitatId)
            val membership = existingMembers.find { it.userId == userId }
                ?: return AppResult.Error(AppError.NotFound("Not a member of this habitat"))

            // Owners cannot leave; they must transfer ownership first
            if (membership.role == EntityHabitatRole.OWNER) {
                return AppResult.Error(AppError.Validation("Owners must transfer ownership before leaving"))
            }

            habitatDao.updateMemberCount(habitatId, -1)
            habitatDao.deleteMembership(habitatId, userId)

            queueSync("habitat_leave", habitatId, "DELETE",
                """{"userId":"$userId","habitatId":"$habitatId"}""")

            Timber.d("Left habitat: $habitatId")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to leave habitat: $habitatId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to leave habitat"))
        }
    }

    override suspend fun getMembers(habitatId: String): AppResult<List<HabitatMembership>> {
        return try {
            val members = habitatDao.getMemberships(habitatId)
            AppResult.Success(members.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to get members for habitat: $habitatId")
            AppResult.Error(AppError.Database(e.message ?: "Failed to get members"))
        }
    }

    override suspend fun updateMemberRole(
        habitatId: String,
        userId: String,
        role: HabitatRole
    ): AppResult<Unit> {
        return try {
            // Find existing membership and update role
            val members = habitatDao.getMemberships(habitatId)
            val existing = members.find { it.userId == userId }
                ?: return AppResult.Error(AppError.NotFound("Member not found"))

            val updated = existing.copy(
                role = role.toEntityRole(),
                syncState = SyncState.PENDING
            )
            habitatDao.upsertMembership(updated)

            queueSync("habitat_member", "${habitatId}_${userId}", "UPDATE",
                """{"habitatId":"$habitatId","userId":"$userId","role":"${role.name}"}""")

            Timber.d("Updated role for user $userId in habitat $habitatId to $role")
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to update member role")
            AppResult.Error(AppError.Database(e.message ?: "Failed to update member role"))
        }
    }

    override suspend fun searchHabitats(query: String): AppResult<List<Habitat>> {
        return try {
            val entities = habitatDao.searchHabitats(query).firstOrNull() ?: emptyList()
            AppResult.Success(entities.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to search habitats: $query")
            AppResult.Error(AppError.Database(e.message ?: "Failed to search habitats"))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LEGACY VIEWMODEL-FACING METHODS
    // These support existing ViewModels that haven't migrated to domain interface.
    // ══════════════════════════════════════════════════════════════════════

    fun getAllHabitats(): Flow<List<HabitatEntity>> {
        return habitatDao.getAllHabitats()
    }

    fun getJoinedHabitats(): Flow<List<HabitatEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitatDao.getJoinedHabitats(userId)
    }

    fun getDiscoverHabitats(limit: Int = 20): Flow<List<HabitatEntity>> {
        val userId = securePreferences.userId ?: return flowOf(emptyList())
        return habitatDao.getDiscoverHabitats(userId)
    }

    fun getHabitatById(habitatId: String): Flow<HabitatEntity?> {
        return habitatDao.getHabitatById(habitatId)
    }

    fun searchHabitatsFlow(query: String): Flow<List<HabitatEntity>> {
        return habitatDao.searchHabitats(query)
    }

    suspend fun createHabitat(habitat: HabitatEntity, creatorId: String) {
        habitatDao.upsert(habitat.copy(syncState = SyncState.PENDING))

        val membership = HabitatMembershipEntity(
            id = UUID.randomUUID().toString(),
            habitatId = habitat.id,
            userId = creatorId,
            role = EntityHabitatRole.OWNER,
            syncState = SyncState.PENDING
        )
        habitatDao.upsertMembership(membership)

        queueSync("habitat", habitat.id, "CREATE",
            moshi.adapter(HabitatEntity::class.java).toJson(habitat))
    }

    suspend fun updateHabitat(habitat: HabitatEntity) {
        habitatDao.upsert(habitat.copy(
            syncState = SyncState.PENDING,
            updatedAt = System.currentTimeMillis()
        ))
        queueSync("habitat", habitat.id, "UPDATE",
            moshi.adapter(HabitatEntity::class.java).toJson(habitat))
    }

    suspend fun joinHabitat(habitatId: String, userId: String) {
        habitatDao.updateMemberCount(habitatId, 1)
        val membership = HabitatMembershipEntity(
            id = UUID.randomUUID().toString(),
            habitatId = habitatId,
            userId = userId,
            role = EntityHabitatRole.MEMBER,
            syncState = SyncState.PENDING
        )
        habitatDao.upsertMembership(membership)
        queueSync("habitat_join", habitatId, "CREATE",
            """{"userId":"$userId","habitatId":"$habitatId"}""")
    }

    suspend fun leaveHabitat(habitatId: String, userId: String) {
        habitatDao.updateMemberCount(habitatId, -1)
        habitatDao.deleteMembership(habitatId, userId)
        queueSync("habitat_leave", habitatId, "DELETE",
            """{"userId":"$userId","habitatId":"$habitatId"}""")
    }

    suspend fun refreshHabitats(): Result<Unit> {
        return try {
            val habitatDtos = apiService.getHabitats()
            val habitats = habitatDtos.map { dto ->
                // Preserve existing local member count if available
                val existingEntity = habitatDao.getHabitatById(dto.id).firstOrNull()
                HabitatEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    coverImageUrl = dto.avatarUrl,
                    memberCount = existingEntity?.memberCount ?: 0,
                    privacy = try {
                        EntityHabitatPrivacy.valueOf(dto.privacyLevel.uppercase())
                    } catch (_: Exception) {
                        EntityHabitatPrivacy.PUBLIC
                    },
                    syncState = SyncState.SYNCED,
                    updatedAt = dto.updatedAt.toEpochMilli()
                )
            }
            habitatDao.upsertAll(habitats)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
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
