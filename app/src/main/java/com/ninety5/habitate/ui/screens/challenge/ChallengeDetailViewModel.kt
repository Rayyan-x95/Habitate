package com.ninety5.habitate.ui.screens.challenge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.analytics.AnalyticsEvent
import com.ninety5.habitate.core.analytics.AnalyticsManager
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.repository.ChallengeRepository
import com.ninety5.habitate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChallengeDetailUiState(
    val challenge: ChallengeEntity? = null,
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
        checkJoinStatus()
        loadLeaderboard()
    }

    private fun loadChallenge() {
        viewModelScope.launch {
            try {
                challengeRepository.getChallengeById(challengeId).collect { challenge ->
                    _uiState.update { it.copy(challenge = challenge, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load challenge: ${e.message}", isLoading = false) }
            }
        }
    }

    private fun checkJoinStatus() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                challengeRepository.getChallengeProgress(challengeId, userId).collect { progress ->
                    _uiState.update { it.copy(isJoined = progress != null) }
                }
            } catch (e: Exception) {
                // Silent failure for join status check - defaults to not joined
                Timber.d(e, "Could not determine join status for challenge $challengeId")
            }
        }
    }

    fun joinChallenge() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                challengeRepository.joinChallenge(challengeId, userId)

                analyticsManager.logEvent(
                    AnalyticsEvent(
                        name = "challenge_joined",
                        userId = userId,
                        properties = mapOf(
                            "challengeId" to challengeId,
                            "habitatId" to (uiState.value.challenge?.habitatId ?: "")
                        )
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to join challenge: ${e.message}") }
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            challengeRepository.getLeaderboard(challengeId)
                .onSuccess { dtos ->
                    val currentUserId = authRepository.getCurrentUserId()
                    val entries = dtos.map { dto ->
                        LeaderboardEntry(
                            rank = dto.rank,
                            userName = dto.displayName,
                            avatarUrl = dto.avatarUrl,
                            score = dto.score,
                            isCurrentUser = dto.userId == currentUserId
                        )
                    }
                    _uiState.update { it.copy(leaderboard = entries) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Failed to load leaderboard: ${e.message}") }
                }
        }
    }
}
