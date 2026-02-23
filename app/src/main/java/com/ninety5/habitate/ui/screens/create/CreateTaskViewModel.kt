package com.ninety5.habitate.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Task
import com.ninety5.habitate.domain.model.TaskPriority
import com.ninety5.habitate.domain.model.TaskStatus
import com.ninety5.habitate.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class CreateTaskUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    fun createTask(title: String, description: String, dueAt: Instant?, priority: TaskPriority, recurrenceRule: String?) {
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val now = Instant.now()
            val task = Task(
                id = UUID.randomUUID().toString(),
                userId = "", // Repository fills from SecurePreferences
                title = title,
                description = description.ifBlank { null },
                dueAt = dueAt,
                recurrenceRule = recurrenceRule,
                priority = priority,
                status = TaskStatus.PENDING,
                categoryId = null,
                linkedEntityId = null,
                linkedEntityType = null,
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
            
            when (val result = taskRepository.createTask(task)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
