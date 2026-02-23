package com.ninety5.habitate.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.HabitatPrivacy
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.HabitatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateHabitatUiState(
    val name: String = "",
    val description: String = "",
    val privacy: HabitatPrivacy = HabitatPrivacy.PUBLIC,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateHabitatViewModel @Inject constructor(
    private val habitatRepository: HabitatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitatUiState())
    val uiState: StateFlow<CreateHabitatUiState> = _uiState.asStateFlow()

    fun onNameChange(newValue: String) {
        _uiState.update { it.copy(name = newValue) }
    }

    fun onDescriptionChange(newValue: String) {
        _uiState.update { it.copy(description = newValue) }
    }

    fun onPrivacyChange(newValue: HabitatPrivacy) {
        _uiState.update { it.copy(privacy = newValue) }
    }

    fun createHabitat() {
        val currentState = _uiState.value
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        if (authRepository.getCurrentUserId() == null) {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = habitatRepository.createHabitat(
                name = currentState.name,
                description = currentState.description.ifBlank { null },
                privacy = currentState.privacy.name
            )) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
