package com.ninety5.habitate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val chats: StateFlow<List<ChatEntity>> = chatRepository.chats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableStateFlow<List<MessageWithReactions>>(emptyList())
    val messages: StateFlow<List<MessageWithReactions>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)

    private var messageCollectionJob: Job? = null

    val currentUserId: String? = authRepository.getCurrentUserId()

    init {
        refreshChats()
        viewModelScope.launch {
            chatRepository.typingEvents.collect { event ->
                if (event.chatId == _currentChatId.value) {
                    if (event.isTyping) {
                        _typingUsers.value += event.userId
                    } else {
                        _typingUsers.value -= event.userId
                    }
                }
            }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            chatRepository.refreshChats()
        }
    }

    fun loadMessages(chatId: String) {
        // Cancel previous message collection job when switching chats
        messageCollectionJob?.cancel()
        
        _currentChatId.value = chatId
        _typingUsers.value = emptySet()
        _messages.value = emptyList()
        _error.value = null
        _isLoading.value = true

        messageCollectionJob = viewModelScope.launch {
            try {
                chatRepository.getMessages(chatId).collect {
                    _messages.value = it
                }
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
            }
        }
        viewModelScope.launch {
            try {
                chatRepository.refreshMessages(chatId)
                chatRepository.markMessagesAsRead(chatId)
            } catch (e: Exception) {
                if (_messages.value.isEmpty()) {
                    _error.value = "Service Unavailable: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        val chatId = _currentChatId.value ?: return
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    chatRepository.sendMessage(chatId, content, currentUserId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to send message"
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        val chatId = _currentChatId.value ?: return
        viewModelScope.launch {
            chatRepository.sendTyping(chatId, isTyping)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun toggleMute(chatId: String, isMuted: Boolean) {
        viewModelScope.launch {
            chatRepository.muteChat(chatId, isMuted)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            chatRepository.addReaction(messageId, currentUserId, emoji)
        }
    }

    fun removeReaction(messageId: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            chatRepository.removeReaction(messageId, currentUserId)
        }
    }

    fun markAsRead(messageId: String) {
        viewModelScope.launch {
            chatRepository.markAsRead(messageId)
        }
    }
}
