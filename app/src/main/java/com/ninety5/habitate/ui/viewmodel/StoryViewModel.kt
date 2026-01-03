package com.ninety5.habitate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class StoriesUiState(
    val stories: List<StoryWithUser> = emptyList(),
    val groupedStories: Map<String, List<StoryWithUser>> = emptyMap(), // userId -> stories
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val createError: String? = null
)

sealed class StoryEvent {
    object StoryCreated : StoryEvent()
    data class CreateError(val message: String) : StoryEvent()
}

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoriesUiState())
    val uiState: StateFlow<StoriesUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<StoryEvent>()
    val events: SharedFlow<StoryEvent> = _events.asSharedFlow()
    
    // Properly derived StateFlow for activeStories (for backward compatibility)
    val activeStories: StateFlow<List<StoryWithUser>> = _uiState
        .map { it.stories }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStories()
    }

    fun loadStories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            storyRepository.getActiveStories()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { stories ->
                    val grouped = stories.groupBy { it.story.userId }
                    _uiState.update { 
                        it.copy(
                            stories = stories, 
                            groupedStories = grouped,
                            isLoading = false 
                        ) 
                    }
                }
        }
    }

    fun markAsViewed(storyId: String) {
        viewModelScope.launch {
            storyRepository.recordView(storyId)
        }
    }

    fun muteUser(userId: String) {
        viewModelScope.launch {
            storyRepository.muteUser(userId)
        }
    }
    
    fun refreshStories() {
        loadStories()
    }

    fun createStory(mediaUri: String, caption: String?, visibility: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isCreating = false, createError = "Not logged in") }
                    _events.emit(StoryEvent.CreateError("Not logged in"))
                    return@launch
                }
                
                val now = Instant.now()
                val expiresAt = now.plus(24, ChronoUnit.HOURS)
                
                val visibilityEnum = try {
                    Visibility.valueOf(visibility.uppercase())
                } catch (e: IllegalArgumentException) {
                    Visibility.PUBLIC
                }
                
                val story = StoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    mediaUrl = mediaUri,
                    caption = caption,
                    visibility = visibilityEnum,
                    createdAt = now.toEpochMilli(),
                    expiresAt = expiresAt.toEpochMilli(),
                    syncState = SyncState.PENDING
                )
                storyRepository.createStory(story)
                
                _uiState.update { it.copy(isCreating = false, createError = null) }
                _events.emit(StoryEvent.StoryCreated)
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreating = false, createError = e.message) }
                _events.emit(StoryEvent.CreateError(e.message ?: "Failed to create story"))
            }
        }
    }
}
