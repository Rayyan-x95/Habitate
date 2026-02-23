package com.ninety5.habitate.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.NotificationRepository
import com.ninety5.habitate.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

data class ActivityUiState(
    val notifications: List<NotificationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val userId: String? = null
)

data class NotificationUiModel(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val type: String,
    val targetId: String?
)

fun Notification.toUiModel() = NotificationUiModel(
    id = id,
    title = title,
    message = body,
    timestamp = createdAt.toEpochMilli(),
    isRead = isRead,
    type = type.name,
    targetId = targetId
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<ActivityUiState> = authRepository.currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                // Handle no user case if necessary
                kotlinx.coroutines.flow.flowOf(ActivityUiState(isLoading = false))
            } else {
                notificationRepository.observeNotifications()
                    .map { notifications ->
                        ActivityUiState(
                            notifications = notifications.map { it.toUiModel() },
                            isLoading = false,
                            userId = userId
                        )
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityUiState(isLoading = true)
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }
}
