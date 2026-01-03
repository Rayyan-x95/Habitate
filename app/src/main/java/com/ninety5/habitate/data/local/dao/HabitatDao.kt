package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.HabitatEntity
import kotlinx.coroutines.flow.Flow

import com.ninety5.habitate.data.local.entity.HabitatMembershipEntity
import com.ninety5.habitate.data.local.entity.SyncState

@Dao
interface HabitatDao {
    @Query("SELECT * FROM habitats")
    fun getAllHabitats(): Flow<List<HabitatEntity>>

    @Query("SELECT * FROM habitats WHERE id IN (SELECT habitatId FROM habitat_memberships WHERE userId = :userId)")
    fun getJoinedHabitats(userId: String): Flow<List<HabitatEntity>>

    @Query("SELECT * FROM habitats WHERE id NOT IN (SELECT habitatId FROM habitat_memberships WHERE userId = :userId)")
    fun getDiscoverHabitats(userId: String): Flow<List<HabitatEntity>>

    @Query("SELECT * FROM habitats WHERE id = :id")
    fun getHabitatById(id: String): Flow<HabitatEntity?>

    @Query("SELECT * FROM habitats WHERE name LIKE '%' || :query || '%'")
    fun searchHabitats(query: String): Flow<List<HabitatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembership(membership: HabitatMembershipEntity)

    @Query("DELETE FROM habitat_memberships WHERE habitatId = :habitatId AND userId = :userId")
    suspend fun deleteMembership(habitatId: String, userId: String)

    @Query("DELETE FROM habitats WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM habitats WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncHabitats(): List<HabitatEntity>

    @Query("UPDATE habitats SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)

    @Query("UPDATE habitats SET memberCount = memberCount + :delta WHERE id = :id")
    suspend fun updateMemberCount(id: String, delta: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habitat: HabitatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(habitats: List<HabitatEntity>)
}
