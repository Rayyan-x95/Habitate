package com.ninety5.habitate.data.local.dao

import androidx.room.*
import com.ninety5.habitate.data.local.entity.HabitEntity
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitStreakEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.relation.HabitWithLogs
import com.ninety5.habitate.data.local.relation.HabitWithStreak
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO for habit CRUD operations.
 */
@Dao
interface HabitDao {
    
    // ====================
    // QUERIES
    // ====================
    
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHabits(userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveHabits(userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: String): Flow<HabitEntity?>
    
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitByIdOnce(id: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0")
    suspend fun getActiveHabitsOnce(userId: String): List<HabitEntity>
    
    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitWithLogs(id: String): Flow<HabitWithLogs?>
    
    @Transaction
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0")
    fun getActiveHabitsWithStreaks(userId: String): Flow<List<HabitWithStreak>>
    
    @Query("""
        SELECT * FROM habits 
        WHERE userId = :userId 
        AND isArchived = 0
        AND reminderEnabled = 1
        ORDER BY reminderTime
    """)
    fun getHabitsWithReminders(userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE category = :category AND userId = :userId AND isArchived = 0")
    fun getHabitsByCategory(category: String, userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE syncState = 'PENDING' LIMIT 20")
    suspend fun getPendingSyncHabits(): List<HabitEntity>
    
    // ====================
    // INSERTS & UPDATES
    // ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(habits: List<HabitEntity>)
    
    @Query("UPDATE habits SET isArchived = 1, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun archiveHabit(id: String, now: Long)

    @Query("UPDATE habits SET syncState = :state, updatedAt = :now WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState, now: Long)

    @Query("UPDATE habits SET title = :title, description = :description, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun updateHabit(id: String, title: String, description: String?, now: Long)
    // ====================
    // DELETE
    // ====================
    
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)
    
    suspend fun upsertAndMarkSynced(habit: HabitEntity) {
        upsert(habit.copy(syncState = SyncState.SYNCED))
    }
}

/**
 * DAO for habit completion logs.
 */
@Dao
interface HabitLogDao {
    
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedAt DESC LIMIT 100")
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getLogsForHabitLimited(habitId: String, limit: Int): List<HabitLogEntity>
    
    @Query("""
        SELECT * FROM habit_logs 
        WHERE habitId = :habitId 
        AND DATE(completedAt / 1000, 'unixepoch') = :date
        LIMIT 1
    """)
    suspend fun getLogForDate(habitId: String, date: String): HabitLogEntity?
    
    @Query("""
        SELECT COUNT(*) > 0 FROM habit_logs 
        WHERE habitId = :habitId 
        AND DATE(completedAt / 1000, 'unixepoch') = DATE('now')
    """)
    fun isCompletedToday(habitId: String): Flow<Boolean>
    
    @Query("""
        SELECT * FROM habit_logs 
        WHERE userId = :userId 
        AND completedAt >= :since
        ORDER BY completedAt DESC
    """)
    fun getRecentLogs(userId: String, since: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE userId = :userId AND completedAt BETWEEN :start AND :end")
    suspend fun getLogsBetween(userId: String, start: Long, end: Long): List<HabitLogEntity>
    
    @Query("SELECT * FROM habit_logs WHERE syncState = 'PENDING' LIMIT 50")
    suspend fun getPendingSyncLogs(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE id = :id")
    suspend fun getLogByIdOnce(id: String): HabitLogEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HabitLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<HabitLogEntity>)
    
    @Query("DELETE FROM habit_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteByHabitId(habitId: String)
    
    @Query("UPDATE habit_logs SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)
    
    suspend fun insertAndMarkSynced(log: HabitLogEntity) {
        insert(log.copy(syncState = SyncState.SYNCED))
    }
}

/**
 * DAO for habit streak tracking.
 */
@Dao
interface HabitStreakDao {
    
    @Query("SELECT * FROM habit_streaks WHERE habitId = :habitId")
    fun getStreak(habitId: String): Flow<HabitStreakEntity?>
    
    @Query("SELECT * FROM habit_streaks WHERE habitId = :habitId")
    suspend fun getStreakOnce(habitId: String): HabitStreakEntity?
    
    @Query("SELECT * FROM habit_streaks WHERE userId = :userId ORDER BY currentStreak DESC LIMIT 1")
    fun getLongestCurrentStreak(userId: String): Flow<HabitStreakEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: HabitStreakEntity)
    
    @Query("DELETE FROM habit_streaks WHERE habitId = :habitId")
    suspend fun deleteByHabitId(habitId: String)

    suspend fun incrementStreak(habitId: String, userId: String, date: String): Boolean {
        val existing = getStreakOnce(habitId)
        val now = System.currentTimeMillis()
        
        if (existing == null) {
            // First completion
            upsert(HabitStreakEntity(
                habitId = habitId,
                userId = userId,
                currentStreak = 1,
                longestStreak = 1,
                lastCompletedDate = date,
                totalCompletions = 1,
                updatedAt = java.time.Instant.ofEpochMilli(now)
            ))
        } else {
            // Check if streak continues
            val yesterday = LocalDate.parse(date).minusDays(1).toString()
            val newStreak = if (existing.lastCompletedDate == yesterday) {
                existing.currentStreak + 1
            } else {
                1 // Streak broken, restart
            }
            
            upsert(existing.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, existing.longestStreak),
                lastCompletedDate = date,
                totalCompletions = existing.totalCompletions + 1,
                updatedAt = java.time.Instant.ofEpochMilli(now)
            ))
        }
        return true
    }
}
