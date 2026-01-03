package com.ninety5.habitate.ui.screens.habitats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.repository.FeedRepository
import com.ninety5.habitate.data.repository.HabitatRepository
import com.ninety5.habitate.data.repository.ChallengeRepository
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.ui.screens.feed.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HabitatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitatRepository: HabitatRepository,
    private val feedRepository: FeedRepository,
    private val challengeRepository: ChallengeRepository,
    private val authRepository: com.ninety5.habitate.data.repository.AuthRepository
) : ViewModel() {

    private val habitatId: String = checkNotNull(savedStateHandle["habitatId"])

    private val _uiState = MutableStateFlow(HabitatDetailUiState())
    val uiState: StateFlow<HabitatDetailUiState> = _uiState.asStateFlow()

    init {
        loadHabitat()
        loadActiveChallenge()
        loadPosts()
    }

    private fun loadHabitat() {
        viewModelScope.launch {
            habitatRepository.getHabitatById(habitatId).collect { habitat ->
                _uiState.update { it.copy(habitat = habitat) }
            }
        }
    }

    private fun loadActiveChallenge() {
        viewModelScope.launch {
            challengeRepository.getAllChallenges().collect { challenges ->
                val active = challenges.find { 
                    it.habitatId == habitatId && it.endDate.isAfter(Instant.now()) 
                }
                _uiState.update { it.copy(activeChallenge = active) }
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            feedRepository.getPostsByHabitat(habitatId).collect { postsWithAuthors ->
                val postUiModels = postsWithAuthors.map { it.toUiModel() }
                _uiState.update { it.copy(posts = postUiModels, isLoading = false) }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            feedRepository.toggleLike(userId, postId)
        }
    }
}

data class HabitatDetailUiState(
    val habitat: HabitatEntity? = null,
    val activeChallenge: ChallengeEntity? = null,
    val posts: List<PostUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
