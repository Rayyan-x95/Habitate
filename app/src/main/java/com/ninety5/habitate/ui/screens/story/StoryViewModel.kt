package com.ninety5.habitate.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.repository.StoryRepository
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

    val activeStories: StateFlow<List<StoryWithUser>> = repository.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createStory(mediaUri: String) {
        viewModelScope.launch {
            repository.createStory(mediaUri)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshStories()
        }
    }
}
