package com.ninety5.habitate.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    val archivedItems: Flow<PagingData<TimelineItem>> = timelineRepository.getArchivedTimeline()
        .cachedIn(viewModelScope)

    fun restoreItem(item: TimelineItem) {
        viewModelScope.launch {
            timelineRepository.restoreItem(item.id, item.type)
        }
    }
}
