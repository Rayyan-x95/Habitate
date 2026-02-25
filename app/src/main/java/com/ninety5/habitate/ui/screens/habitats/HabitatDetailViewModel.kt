package com.ninety5.habitate.ui.screens.habitats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Challenge
import com.ninety5.habitate.domain.model.Habitat
import com.ninety5.habitate.domain.repository.FeedRepository
import com.ninety5.habitate.domain.repository.HabitatRepository
import com.ninety5.habitate.domain.repository.ChallengeRepository
import com.ninety5.habitate.ui.screens.feed.PostUiModel
import com.ninety5.habitate.ui.screens.feed.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitatRepository: HabitatRepository,
    private val feedRepository: FeedRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _habitatId = MutableStateFlow<String>(checkNotNull(savedStateHandle["habitatId"]))

    private val _uiState = MutableStateFlow(HabitatDetailUiState())
    val uiState: StateFlow<HabitatDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _habitatId.flatMapLatest { id ->
                habitatRepository.observeHabitat(id)
            }.collectLatest { habitat ->
                _uiState.update { it.copy(habitat = habitat) }
            }
        }

        viewModelScope.launch {
            _habitatId.flatMapLatest { id ->
                challengeRepository.observeActiveChallenges()
            }.collectLatest { challenges ->
                val active = challenges.find {
                    it.habitatId == _habitatId.value && it.endDate.isAfter(Instant.now())
                }
                _uiState.update { it.copy(activeChallenge = active) }
            }
        }

        viewModelScope.launch {
            _habitatId.flatMapLatest { id ->
                feedRepository.getPostsByHabitat(id)
            }.collectLatest { posts ->
                val postUiModels = posts.map { it.toUiModel() }
                _uiState.update { it.copy(posts = postUiModels, isLoading = false) }
            }
        }
    }

    fun loadHabitat(id: String) {
        if (_habitatId.value != id) {
            _uiState.update { it.copy(isLoading = true) }
            _habitatId.value = id
        }
    }

    fun toggleLike(postId: String, reactionType: String? = null) {
        viewModelScope.launch {
            when (val result = feedRepository.toggleLike(postId, reactionType)) {
                is AppResult.Success -> { /* Optimistic update handled by DB Flow */ }
                is AppResult.Error -> {
                    Timber.e("Failed to toggle like: ${result.error.message}")
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HabitatDetailUiState(
    val habitat: Habitat? = null,
    val activeChallenge: Challenge? = null,
    val posts: List<PostUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
