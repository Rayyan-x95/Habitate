package com.ninety5.habitate.util

import javax.inject.Inject
import javax.inject.Singleton

interface FeatureFlags {
    val isChatEnabled: Boolean
    val isStoriesEnabled: Boolean
    val isFocusModeEnabled: Boolean
    val isAiPlannerEnabled: Boolean
}

@Singleton
class FeatureFlagsImpl @Inject constructor() : FeatureFlags {
    // Feature flags are currently local toggles.
    // To connect to Firebase Remote Config in the future, inject FirebaseRemoteConfig here
    // and read values from Remote Config with local defaults.
    
    override val isChatEnabled: Boolean = false
    override val isStoriesEnabled: Boolean = false
    override val isFocusModeEnabled: Boolean = true
    override val isAiPlannerEnabled: Boolean = true
}
