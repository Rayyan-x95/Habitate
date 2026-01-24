package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllEntries(userId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllEntriesSync(userId: String): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun getEntryById(id: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryByIdSync(id: String): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE id = :id AND userId = :userId")
    fun getEntryByIdAndUserId(id: String, userId: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE id = :id AND userId = :userId")
    suspend fun getEntryByIdAndUserIdSync(id: String, userId: String): JournalEntryEntity?

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM journal_entries WHERE id = :id AND userId = :userId")
    suspend fun deleteByIdAndUserId(id: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity)

    @Query("""
        SELECT * FROM journal_entries 
        WHERE userId = :userId AND date >= :startTime AND date < :endTime 
        ORDER BY date DESC
    """)
    fun getEntriesForDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<JournalEntryEntity>>

    @Query("""
        SELECT * FROM journal_entries 
        WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun searchEntries(userId: String, query: String): Flow<List<JournalEntryEntity>>
    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND mood = :mood ORDER BY date DESC")
    fun getEntriesByMood(userId: String, mood: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT DISTINCT mood FROM journal_entries WHERE userId = :userId AND mood IS NOT NULL")
    fun getUniqueMoods(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    fun getEntryCount(userId: String): Flow<Int>
}
