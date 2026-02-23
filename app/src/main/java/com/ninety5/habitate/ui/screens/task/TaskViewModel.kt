package com.ninety5.habitate.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Task
import com.ninety5.habitate.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    val tasks: StateFlow<List<Task>> = taskRepository.observeAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            when (val result = taskRepository.completeTask(taskId)) {
                is AppResult.Success -> { /* Room Flow auto-updates */ }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.error.message)
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val result = if (task.status == com.ninety5.habitate.domain.model.TaskStatus.COMPLETED) {
                // Re-open the task
                taskRepository.updateTask(task.copy(status = com.ninety5.habitate.domain.model.TaskStatus.PENDING))
            } else {
                taskRepository.completeTask(task.id)
            }
            when (result) {
                is AppResult.Success -> { /* Room Flow auto-updates */ }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.error.message)
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            when (val result = taskRepository.archiveTask(taskId)) {
                is AppResult.Success -> { /* Room Flow auto-updates */ }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.error.message)
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
