package com.ninety5.habitate.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.model.Conversation
import com.ninety5.habitate.domain.model.Message
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val chats: StateFlow<List<Conversation>> = repository.observeConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUserId: String?
        get() = authRepository.getCurrentUserId()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val messages: StateFlow<List<Message>> = _currentChatId
        .asStateFlow()
        .flatMapLatest { chatId ->
            if (chatId != null) {
                repository.observeMessages(chatId)
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
            repository.observeTypingEvents().collect { typingEvent ->
                if (typingEvent.conversationId == _currentChatId.value && typingEvent.isTyping) {
                    _typingUsers.value = _typingUsers.value + typingEvent.userId
                } else if (typingEvent.conversationId == _currentChatId.value && !typingEvent.isTyping) {
                    _typingUsers.value = _typingUsers.value - typingEvent.userId
                }
            }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.refreshConversations()) {
                is AppResult.Success -> { /* conversations updated via Flow */ }
                is AppResult.Error -> {
                    _error.value = result.error.message
                }
                is AppResult.Loading -> { /* no-op */ }
            }
            _isLoading.value = false
        }
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        viewModelScope.launch {
            repository.refreshMessages(chatId)
        }
    }

    fun sendMessage(chatId: String, content: String) {
        viewModelScope.launch {
            when (val result = repository.sendMessage(chatId, content, null)) {
                is AppResult.Success -> { /* Message sent, UI updated via Flow */ }
                is AppResult.Error -> {
                    _error.value = result.error.message
                }
                is AppResult.Loading -> { /* no-op */ }
            }
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
            repository.muteConversation(chatId, isMuted)
        }
    }

    fun retryMessage(messageId: String) {
        viewModelScope.launch {
            // Retry logic would go here
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            when (val result = repository.addReaction(messageId, emoji)) {
                is AppResult.Success -> { /* Reaction added via Flow */ }
                is AppResult.Error -> {
                    _error.value = result.error.message
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteMessage(messageId)) {
                is AppResult.Success -> { /* Message deleted via Flow */ }
                is AppResult.Error -> {
                    _error.value = result.error.message
                }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
