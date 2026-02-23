package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Habitat
import com.ninety5.habitate.domain.model.HabitatMembership
import com.ninety5.habitate.domain.model.HabitatRole
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for habitat (community) operations.
 */
interface HabitatRepository {
    fun observeMyHabitats(): Flow<List<Habitat>>
    fun observeHabitat(habitatId: String): Flow<Habitat?>
    fun discoverHabitats(): Flow<List<Habitat>>
    suspend fun getHabitat(habitatId: String): AppResult<Habitat>
    suspend fun createHabitat(name: String, description: String?, privacy: String): AppResult<Habitat>
    suspend fun updateHabitat(habitat: Habitat): AppResult<Unit>
    suspend fun deleteHabitat(habitatId: String): AppResult<Unit>
    suspend fun joinHabitat(habitatId: String): AppResult<Unit>
    suspend fun leaveHabitat(habitatId: String): AppResult<Unit>
    suspend fun getMembers(habitatId: String): AppResult<List<HabitatMembership>>
    suspend fun updateMemberRole(habitatId: String, userId: String, role: HabitatRole): AppResult<Unit>
    suspend fun searchHabitats(query: String): AppResult<List<Habitat>>
}
