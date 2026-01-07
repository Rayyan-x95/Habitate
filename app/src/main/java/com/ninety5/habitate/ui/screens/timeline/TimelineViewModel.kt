package com.ninety5.habitate.ui.screens.timeline

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ninety5.habitate.core.export.TimelineExporter
import com.ninety5.habitate.data.local.view.TimelineItem
import com.ninety5.habitate.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExportFormat { JSON, PDF }

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val timelineExporter: TimelineExporter
) : ViewModel() {

    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineItems: Flow<PagingData<TimelineItem>> = combine(
        _filterType,
        _searchQuery
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        timelineRepository.getTimeline(filter, query)
    }.cachedIn(viewModelScope)

    fun setFilter(type: String?) {
        _filterType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun exportTimeline(uri: Uri, format: ExportFormat) {
        viewModelScope.launch {
            val items = timelineRepository.getAllTimelineItems().first()
            val result = when (format) {
                ExportFormat.JSON -> timelineExporter.exportToJson(items, uri)
                ExportFormat.PDF -> timelineExporter.exportToPdf(items, uri)
            }
            // In a real app, we would expose this result to the UI via a SharedFlow/Channel
        }
    }
}
