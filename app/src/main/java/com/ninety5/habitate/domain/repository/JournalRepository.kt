package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.domain.model.JournalMood
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Domain contract for journal operations.
 */
interface JournalRepository {
    fun observeAllEntries(): Flow<List<JournalEntry>>
    fun observeEntriesForDate(date: LocalDate): Flow<List<JournalEntry>>
    fun searchEntries(query: String): Flow<List<JournalEntry>>
    suspend fun getEntry(entryId: String): AppResult<JournalEntry>
    suspend fun createEntry(entry: JournalEntry): AppResult<JournalEntry>
    suspend fun updateEntry(entry: JournalEntry): AppResult<Unit>
    suspend fun deleteEntry(entryId: String): AppResult<Unit>
    suspend fun getEntriesByMood(mood: JournalMood): AppResult<List<JournalEntry>>
    suspend fun exportToJson(): AppResult<String>
}
