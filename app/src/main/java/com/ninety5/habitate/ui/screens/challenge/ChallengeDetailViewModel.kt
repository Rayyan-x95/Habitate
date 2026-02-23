package com.ninety5.habitate.ui.screens.challenge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.analytics.AnalyticsEvent
import com.ninety5.habitate.core.analytics.AnalyticsManager
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Challenge
import com.ninety5.habitate.domain.repository.ChallengeRepository
import com.ninety5.habitate.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChallengeDetailUiState(
    val challenge: Challenge? = null,
    val isJoined: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val leaderboard: List<LeaderboardEntry> = emptyList()
)

data class LeaderboardEntry(
    val rank: Int,
    val userName: String,
    val avatarUrl: String?,
    val score: Double,
    val isCurrentUser: Boolean = false
)

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val challengeRepository: ChallengeRepository,
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val challengeId: String = checkNotNull(savedStateHandle["challengeId"])
    
    private val _uiState = MutableStateFlow(ChallengeDetailUiState())
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    init {
        loadChallenge()
        loadLeaderboard()
    }

    private fun loadChallenge() {
        viewModelScope.launch {
            when (val result = challengeRepository.getChallenge(challengeId)) {
                is AppResult.Success -> {
                    val challenge = result.data
                    _uiState.update { it.copy(
                        challenge = challenge,
                        isJoined = challenge.isJoined,
                        isLoading = false
                    ) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = "Failed to load challenge: ${result.error.message}", isLoading = false) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun joinChallenge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = challengeRepository.joinChallenge(challengeId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    val userId = authRepository.getCurrentUserId()
                    analyticsManager.logEvent(
                        AnalyticsEvent(
                            name = "challenge_joined",
                            userId = userId ?: "",
                            properties = mapOf(
                                "challengeId" to challengeId,
                                "habitatId" to (uiState.value.challenge?.habitatId ?: "")
                            )
                        )
                    )
                    loadChallenge() // Refresh to update join status
                    loadLeaderboard() // Refresh leaderboard
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to join challenge: ${result.error.message}") }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            when (val result = challengeRepository.getLeaderboard(challengeId)) {
                is AppResult.Success -> {
                    val currentUserId = authRepository.getCurrentUserId()
                    val entries = result.data.map { progress ->
                        LeaderboardEntry(
                            rank = progress.rank ?: 0,
                            userName = progress.userId,
                            avatarUrl = null,
                            score = progress.currentValue,
                            isCurrentUser = progress.userId == currentUserId
                        )
                    }
                    _uiState.update { it.copy(leaderboard = entries) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(error = "Failed to load leaderboard: ${result.error.message}") }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
