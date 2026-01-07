package com.ninety5.habitate.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ninety5.habitate.util.FeatureFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    val featureFlags: FeatureFlags
) : ViewModel()

