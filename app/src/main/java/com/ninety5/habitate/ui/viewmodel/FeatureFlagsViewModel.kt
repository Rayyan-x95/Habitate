package com.ninety5.habitate.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ninety5.habitate.core.utils.DebugLogger
import com.ninety5.habitate.util.FeatureFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    val featureFlags: FeatureFlags
) : ViewModel() {
    init {
        // #region agent log
        DebugLogger.log(
            "FeatureFlagsViewModel.kt:init",
            "FeatureFlagsViewModel initialized",
            mapOf("isChatEnabled" to featureFlags.isChatEnabled, "isStoriesEnabled" to featureFlags.isStoriesEnabled),
            "B"
        )
        // #endregion
    }
}

