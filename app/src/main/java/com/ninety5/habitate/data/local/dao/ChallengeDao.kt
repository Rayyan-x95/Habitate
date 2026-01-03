package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.local.entity.ChallengeProgressEntity
import com.ninety5.habitate.data.local.entity.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY startDate DESC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    fun getChallengeById(id: String): Flow<ChallengeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(challenge: ChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ChallengeProgressEntity)

    @Query("SELECT * FROM challenge_progress WHERE challengeId = :challengeId AND userId = :userId")
    fun getProgress(challengeId: String, userId: String): Flow<ChallengeProgressEntity?>

    @Transaction
    suspend fun upsertAndMarkSynced(challenge: ChallengeEntity) {
        upsert(challenge.copy(syncState = SyncState.SYNCED))
    }

    @Query("UPDATE challenges SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)
}
