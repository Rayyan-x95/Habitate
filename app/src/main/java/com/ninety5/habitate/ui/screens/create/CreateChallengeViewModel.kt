package com.ninety5.habitate.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class CreateChallengeUiState(
    val title: String = "",
    val description: String = "",
    val metricType: String = "DISTANCE", // DISTANCE, COUNT, DURATION
    val targetValue: String = "",
    val durationDays: String = "7",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChallengeUiState())
    val uiState: StateFlow<CreateChallengeUiState> = _uiState.asStateFlow()

    fun onTitleChange(newValue: String) {
        _uiState.update { it.copy(title = newValue) }
    }

    fun onDescriptionChange(newValue: String) {
        _uiState.update { it.copy(description = newValue) }
    }

    fun onMetricTypeChange(newValue: String) {
        _uiState.update { it.copy(metricType = newValue) }
    }

    fun onTargetValueChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            _uiState.update { it.copy(targetValue = newValue) }
        }
    }

    fun onDurationDaysChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(durationDays = newValue) }
        }
    }

    fun createChallenge() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() || currentState.targetValue.isBlank()) {
            _uiState.update { it.copy(error = "Title and Target Value are required") }
            return
        }

        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val startDate = Instant.now()
                val days = currentState.durationDays.toLongOrNull() ?: 7L
                val endDate = startDate.plus(days, ChronoUnit.DAYS)

                val challenge = ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = currentState.title,
                    description = currentState.description.ifBlank { null },
                    metricType = currentState.metricType,
                    targetValue = currentState.targetValue.toDoubleOrNull() ?: 0.0,
                    startDate = startDate,
                    endDate = endDate,
                    creatorId = userId,
                    habitatId = null, // Global challenge for now, or add habitat selection later
                    syncState = SyncState.PENDING,
                    createdAt = Instant.now()
                )

                challengeRepository.createChallenge(challenge)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
