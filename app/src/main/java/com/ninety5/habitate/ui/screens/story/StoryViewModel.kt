package com.ninety5.habitate.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.domain.model.Story
import com.ninety5.habitate.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repository: StoryRepository
) : ViewModel() {

    val activeStories: StateFlow<List<Story>> = repository.observeActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createStory(mediaUri: String, caption: String? = null) {
        viewModelScope.launch {
            repository.createStory(mediaUri, caption, "PUBLIC")
        }
    }

    fun markAsSeen(storyId: String) {
        viewModelScope.launch {
            repository.markAsViewed(storyId)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshStories()
        }
    }
}
