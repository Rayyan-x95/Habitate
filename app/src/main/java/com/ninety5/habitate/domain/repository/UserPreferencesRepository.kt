package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for user preferences operations.
 */
interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}
