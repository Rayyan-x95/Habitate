package com.ninety5.habitate.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val chats: StateFlow<List<ChatEntity>> = repository.chats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUserId: String?
        get() = authRepository.getCurrentUserId()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val messages: StateFlow<List<MessageWithReactions>> = _currentChatId
        .asStateFlow()
        .flatMapLatest { chatId ->
            if (chatId != null) {
                repository.getMessages(chatId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeRealtime()
            repository.typingEvents.collect { typingEvent ->
                if (typingEvent.chatId == _currentChatId.value && typingEvent.isTyping) {
                    _typingUsers.value = _typingUsers.value + typingEvent.userId
                } else if (typingEvent.chatId == _currentChatId.value && !typingEvent.isTyping) {
                    _typingUsers.value = _typingUsers.value - typingEvent.userId
                }
            }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshChats()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh chats"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        viewModelScope.launch {
            try {
                repository.refreshMessages(chatId)
            } catch (e: Exception) {
                // Handle error silently or show snackbar
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.sendMessage(chatId, content, userId, null)
        }
    }

    fun sendTyping(isTyping: Boolean) {
        val chatId = _currentChatId.value ?: return
        viewModelScope.launch {
            repository.sendTyping(chatId, isTyping)
        }
    }

    fun toggleMute(chatId: String, isMuted: Boolean) {
        viewModelScope.launch {
            repository.muteChat(chatId, isMuted)
        }
    }

    fun retryMessage(messageId: String) {
        viewModelScope.launch {
            // Retry logic would go here
        }
    }
}
