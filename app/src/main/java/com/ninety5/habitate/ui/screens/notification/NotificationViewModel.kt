package com.ninety5.habitate.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.NotificationEntity
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        refreshNotifications()
    }

    private fun loadNotifications() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            notificationRepository.getAllNotifications(userId).collect { notifications ->
                _uiState.update { it.copy(notifications = notifications) }
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = notificationRepository.refreshNotifications()
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                ) 
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
}
