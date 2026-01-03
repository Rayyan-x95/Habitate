package com.ninety5.habitate.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.dao.DailySummaryDao
import com.ninety5.habitate.data.local.entity.DailySummaryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyCheckInViewModel @Inject constructor(
    private val dailySummaryDao: DailySummaryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        loadTodaySummary()
    }

    private fun loadTodaySummary() {
        viewModelScope.launch {
            dailySummaryDao.getSummary(LocalDate.now()).collect { summary ->
                if (summary != null) {
                    _uiState.update { 
                        it.copy(
                            mood = summary.mood,
                            notes = summary.notes,
                            existingSummary = summary
                        ) 
                    }
                }
            }
        }
    }

    fun setMood(mood: String) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveCheckIn() {
        viewModelScope.launch {
            val current = _uiState.value.existingSummary ?: DailySummaryEntity(
                date = LocalDate.now(),
                steps = 0,
                caloriesBurned = 0.0,
                distanceMeters = 0.0,
                activeMinutes = 0
            )
            
            val updated = current.copy(
                mood = _uiState.value.mood,
                notes = _uiState.value.notes
            )
            
            dailySummaryDao.upsert(updated)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

data class CheckInUiState(
    val mood: String? = null,
    val notes: String? = null,
    val existingSummary: DailySummaryEntity? = null,
    val isSaved: Boolean = false
)
