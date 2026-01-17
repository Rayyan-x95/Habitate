package com.ninety5.habitate.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import com.ninety5.habitate.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<JournalEvent>()
    val events: SharedFlow<JournalEvent> = _events.asSharedFlow()

    val entries: StateFlow<List<JournalEntryEntity>> = journalRepository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                journalRepository.getAllEntries().collect { entries ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            entries = entries,
                            entriesGroupedByDate = entries.groupBy { entry ->
                                java.time.Instant.ofEpochMilli(entry.date)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        viewModelScope.launch {
            journalRepository.getEntriesForDate(date).collect { entries ->
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
            try {
                journalRepository.createEntry(
                    title = title,
                    content = content,
                    mood = mood,
                    tags = tags,
                    mediaUrls = mediaUrls
                )
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(JournalEvent.EntrySaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(JournalEvent.Error(e.message ?: "Failed to save entry"))
            }
        }
    }

    fun updateEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                journalRepository.updateEntry(entry)
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(JournalEvent.EntrySaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(JournalEvent.Error(e.message ?: "Failed to update entry"))
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                journalRepository.deleteEntry(entryId)
                _events.emit(JournalEvent.EntryDeleted)
            } catch (e: Exception) {
                _events.emit(JournalEvent.Error(e.message ?: "Failed to delete entry"))
            }
        }
    }

    fun searchEntries(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadEntries()
            return
        }
        viewModelScope.launch {
            journalRepository.searchEntries(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            try {
                val json = journalRepository.exportToJson()
                _events.emit(JournalEvent.ExportReady(json))
            } catch (e: Exception) {
                _events.emit(JournalEvent.Error(e.message ?: "Export failed"))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setEditingEntry(entry: JournalEntryEntity?) {
        _uiState.update { it.copy(editingEntry = entry) }
    }
}

data class JournalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val entries: List<JournalEntryEntity> = emptyList(),
    val entriesGroupedByDate: Map<LocalDate, List<JournalEntryEntity>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDateEntries: List<JournalEntryEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<JournalEntryEntity> = emptyList(),
    val editingEntry: JournalEntryEntity? = null
)

sealed class JournalEvent {
    object EntrySaved : JournalEvent()
    object EntryDeleted : JournalEvent()
    data class ExportReady(val json: String) : JournalEvent()
    data class Error(val message: String) : JournalEvent()
}
