package com.ninety5.habitate.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.domain.model.JournalMood
import com.ninety5.habitate.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<JournalEvent>()
    val events: SharedFlow<JournalEvent> = _events.asSharedFlow()

    private var entriesJob: Job? = null
    private var dateCollectionJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadEntries()
    }

    private fun loadEntries() {
        entriesJob?.cancel()
        entriesJob = viewModelScope.launch {
            journalRepository.observeAllEntries()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> 
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { entries ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            entries = entries,
                            entriesGroupedByDate = entries.groupBy { entry ->
                                entry.createdAt
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
                }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        dateCollectionJob?.cancel()
        dateCollectionJob = viewModelScope.launch {
            journalRepository.observeEntriesForDate(date).collect { entries ->
                _uiState.update { it.copy(selectedDateEntries = entries) }
            }
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth) }
    }

    fun createEntry(
        title: String?,
        content: String,
        mood: String?,
        tags: List<String> = emptyList(),
        mediaUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val moodEnum = mood?.let {
                try { JournalMood.valueOf(it.uppercase()) } catch (_: Exception) { null }
            }

            val entry = JournalEntry(
                id = UUID.randomUUID().toString(),
                userId = "",  // Repository will set from SecurePreferences
                title = title,
                content = content,
                mood = moodEnum,
                tags = tags,
                mediaUrls = mediaUrls,
                isPrivate = true,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            when (val result = journalRepository.createEntry(entry)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(JournalEvent.EntrySaved)
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.error.message) }
                    _events.emit(JournalEvent.Error(result.error.message))
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val entryWithTimestamp = entry.copy(updatedAt = Instant.now())
            when (val result = journalRepository.updateEntry(entryWithTimestamp)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(JournalEvent.EntrySaved)
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.error.message) }
                    _events.emit(JournalEvent.Error(result.error.message))
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            when (val result = journalRepository.deleteEntry(entryId)) {
                is AppResult.Success -> {
                    _events.emit(JournalEvent.EntryDeleted)
                }
                is AppResult.Error -> {
                    _events.emit(JournalEvent.Error(result.error.message))
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun searchEntries(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            journalRepository.searchEntries(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            when (val result = journalRepository.exportToJson()) {
                is AppResult.Success -> {
                    _events.emit(JournalEvent.ExportReady(result.data))
                }
                is AppResult.Error -> {
                    _events.emit(JournalEvent.Error(result.error.message))
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setEditingEntry(entry: JournalEntry?) {
        _uiState.update { it.copy(editingEntry = entry) }
    }
}

data class JournalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val entries: List<JournalEntry> = emptyList(),
    val entriesGroupedByDate: Map<LocalDate, List<JournalEntry>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDateEntries: List<JournalEntry> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<JournalEntry> = emptyList(),
    val editingEntry: JournalEntry? = null
)

sealed class JournalEvent {
    object EntrySaved : JournalEvent()
    object EntryDeleted : JournalEvent()
    data class ExportReady(val json: String) : JournalEvent()
    data class Error(val message: String) : JournalEvent()
}
