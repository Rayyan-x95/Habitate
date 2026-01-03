package com.ninety5.habitate.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.dao.FocusDao
import com.ninety5.habitate.data.local.entity.FocusSessionEntity
import com.ninety5.habitate.data.local.entity.FocusSessionStatus
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.util.audio.AmbientSoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusDao: FocusDao,
    private val soundPlayer: AmbientSoundPlayer,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isTimerRunning = true) }
        // Resume sound if selected
        if (_uiState.value.selectedSound != null) {
            soundPlayer.resume()
        }
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSeconds > 0) {
                delay(1000L)
                _uiState.update {
                    it.copy(timeLeftSeconds = it.timeLeftSeconds - 1)
                }
            }
            completeSession()
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
        soundPlayer.pause()
    }

    fun stopSession() {
        pauseTimer()
        soundPlayer.stop()
        _uiState.update { it.copy(timeLeftSeconds = it.initialDuration, isTimerRunning = false, selectedSound = null) }
    }

    private fun completeSession() {
        pauseTimer()
        soundPlayer.stop()
        saveSession()
        _uiState.update { it.copy(isSessionComplete = true, isTimerRunning = false) }
    }

    private fun saveSession() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val session = FocusSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                startTime = Instant.now().minusSeconds(_uiState.value.initialDuration - _uiState.value.timeLeftSeconds),
                endTime = Instant.now(),
                durationSeconds = _uiState.value.initialDuration - _uiState.value.timeLeftSeconds,
                status = FocusSessionStatus.COMPLETED,
                soundTrack = _uiState.value.selectedSound,
                rating = null,
                syncState = SyncState.PENDING,
                updatedAt = Instant.now()
            )
            focusDao.upsert(session)
        }
    }

    fun setDuration(minutes: Int) {
        val seconds = minutes * 60L
        _uiState.update { it.copy(initialDuration = seconds, timeLeftSeconds = seconds) }
    }

    fun playSound(soundName: String, resId: Int) {
        if (_uiState.value.selectedSound == soundName) {
            // Toggle off
            soundPlayer.stop()
            _uiState.update { it.copy(selectedSound = null) }
        } else {
            // GUARDRAIL: Validate resource ID before playing
            if (resId == 0) {
                timber.log.Timber.w("Invalid sound resource ID for: $soundName - sound not available")
                _uiState.update { it.copy(error = "Sound '$soundName' is not available yet") }
                return
            }
            soundPlayer.play(resId, soundName)
            _uiState.update { it.copy(selectedSound = soundName, error = null) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun resetSession() {
        _uiState.update { 
            it.copy(
                isSessionComplete = false, 
                timeLeftSeconds = it.initialDuration,
                isTimerRunning = false
            ) 
        }
    }
}

data class FocusUiState(
    val isTimerRunning: Boolean = false,
    val initialDuration: Long = 25 * 60,
    val timeLeftSeconds: Long = 25 * 60,
    val selectedSound: String? = null,
    val isSessionComplete: Boolean = false,
    val error: String? = null
)
