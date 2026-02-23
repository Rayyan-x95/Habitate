package com.ninety5.habitate.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Task
import com.ninety5.habitate.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _error.value = null
            when (val result = taskRepository.getTask(taskId)) {
                is AppResult.Success -> _task.value = result.data
                is AppResult.Error -> _error.value = result.error.message
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
