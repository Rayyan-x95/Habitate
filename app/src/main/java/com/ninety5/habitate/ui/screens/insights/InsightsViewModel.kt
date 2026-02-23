package com.ninety5.habitate.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.insights.InsightGenerator
import com.ninety5.habitate.domain.model.Insight
import com.ninety5.habitate.domain.repository.InsightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightRepository: InsightRepository,
    private val insightGenerator: InsightGenerator
) : ViewModel() {

    private val _insights = MutableStateFlow<List<Insight>>(emptyList())
    val insights: StateFlow<List<Insight>> = _insights.asStateFlow()

    init {
        loadInsights()
        refreshInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            insightRepository.observeActiveInsights().collectLatest {
                _insights.value = it
            }
        }
    }

    fun refreshInsights() {
        viewModelScope.launch {
            insightGenerator.generateInsights()
        }
    }

    fun dismissInsight(insight: Insight) {
        viewModelScope.launch {
            insightRepository.dismissInsight(insight.id)
        }
    }

    fun actOnInsight(insight: Insight) {
        viewModelScope.launch {
            // Mark as actioned (dismissed) and let the UI navigate if needed
            insightRepository.dismissInsight(insight.id)
        }
    }
    
}
