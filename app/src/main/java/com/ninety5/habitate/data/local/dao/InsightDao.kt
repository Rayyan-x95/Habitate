package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM insights WHERE isDismissed = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveInsights(): Flow<List<InsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: InsightEntity)

    @Query("UPDATE insights SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: String)

    @Query("DELETE FROM insights WHERE createdAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
