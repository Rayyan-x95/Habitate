package com.ninety5.habitate.data.repository

import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.dao.DailySummaryDao
import com.ninety5.habitate.data.local.entity.DailySummaryEntity
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.mapper.toEntity
import com.ninety5.habitate.domain.model.DailySummary
import com.ninety5.habitate.domain.repository.DailyCheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class DailyCheckInRepositoryImpl @Inject constructor(
    private val dailySummaryDao: DailySummaryDao
) : DailyCheckInRepository {

    override fun observeTodaySummary(): Flow<DailySummary?> {
        return dailySummaryDao.getSummary(LocalDate.now()).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveCheckIn(mood: String?, notes: String?): AppResult<Unit> {
        return try {
            val today = LocalDate.now()
            val existing = dailySummaryDao.getSummary(today).firstOrNull()
            val entity = existing?.copy(mood = mood, notes = notes) ?: DailySummaryEntity(
                date = today,
                steps = 0,
                caloriesBurned = 0.0,
                distanceMeters = 0.0,
                activeMinutes = 0,
                mood = mood,
                notes = notes
            )
            dailySummaryDao.upsert(entity)
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to save check-in")
            AppResult.Error(AppError.from(e))
        }
    }
}
