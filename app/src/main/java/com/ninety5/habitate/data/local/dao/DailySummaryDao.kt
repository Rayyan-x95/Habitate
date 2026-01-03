package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    fun getSummary(date: LocalDate): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSummariesBetween(startDate: LocalDate, endDate: LocalDate): List<DailySummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailySummaryEntity)
}
