package com.ninety5.habitate.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.domain.ai.AICoachingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val aiCoachingService: AICoachingService,
    private val userRepository: com.ninety5.habitate.data.repository.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlannerUiState>(PlannerUiState.Loading)
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        loadPlan()
    }

    fun loadPlan() {
        viewModelScope.launch {
            _uiState.value = PlannerUiState.Loading
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    val advice = aiCoachingService.getDailyAdvice(currentUser.id)
                    _uiState.value = PlannerUiState.Success(advice)
                } else {
                    _uiState.value = PlannerUiState.Error("User not logged in")
                }
            } catch (e: Exception) {
                _uiState.value = PlannerUiState.Error("Failed to load plan: ${e.message}")
            }
        }
    }
}

sealed class PlannerUiState {
    object Loading : PlannerUiState()
    data class Success(val advice: String) : PlannerUiState()
    data class Error(val message: String) : PlannerUiState()
}
