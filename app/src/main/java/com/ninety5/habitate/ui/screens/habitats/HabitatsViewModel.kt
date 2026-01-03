package com.ninety5.habitate.ui.screens.habitats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ninety5.habitate.data.repository.HabitatRepository
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.ChallengeRepository
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.combine
import java.time.Instant

@HiltViewModel
class HabitatsViewModel @Inject constructor(
    private val habitatRepository: HabitatRepository,
    private val authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitatsUiState())
    val uiState: StateFlow<HabitatsUiState> = _uiState.asStateFlow()

    init {
        observeHabitats()
    }

    private fun observeHabitats() {
        viewModelScope.launch {
            combine(
                habitatRepository.getJoinedHabitats(),
                habitatRepository.getDiscoverHabitats(),
                challengeRepository.getAllChallenges()
            ) { joined, discover, challenges ->
                Triple(joined, discover, challenges)
            }.collect { (joined, discover, challenges) ->
                _uiState.update {
                    it.copy(
                        myHabitats = joined.map { h -> h.toUiModel(true, challenges) },
                        discoverHabitats = discover.map { h -> h.toUiModel(false, challenges) },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun HabitatEntity.toUiModel(joined: Boolean, challenges: List<ChallengeEntity>): HabitatUiModel {
        val activeChallenge = challenges.find { 
            it.habitatId == id && it.endDate.isAfter(Instant.now()) 
        }
        
        return HabitatUiModel(
            id = id,
            name = name,
            description = description ?: "",
            imageUrl = coverImageUrl,
            memberCount = memberCount,
            privacy = privacy,
            activeChallenge = activeChallenge?.title,
            isJoined = joined
        )
    }

    fun joinHabitat(habitatId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            habitatRepository.joinHabitat(habitatId, userId)
        }
    }

    fun leaveHabitat(habitatId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            habitatRepository.leaveHabitat(habitatId, userId)
        }
    }
}

data class HabitatsUiState(
    val myHabitats: List<HabitatUiModel> = emptyList(),
    val discoverHabitats: List<HabitatUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
