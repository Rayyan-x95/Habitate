package com.ninety5.habitate.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.repository.FocusRepository
import com.ninety5.habitate.util.audio.AmbientSoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val soundPlayer: AmbientSoundPlayer
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
            val elapsed = _uiState.value.initialDuration - _uiState.value.timeLeftSeconds
            when (val result = focusRepository.saveCompletedSession(elapsed, _uiState.value.selectedSound)) {
                is AppResult.Success -> Timber.d("Focus session saved: ${result.data.id}")
                is AppResult.Error -> Timber.e("Failed to save session: ${result.error.message}")
                is AppResult.Loading -> { /* no-op */ }
            }
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
