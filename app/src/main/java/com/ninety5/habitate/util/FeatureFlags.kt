package com.ninety5.habitate.util

import com.ninety5.habitate.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified feature flags for the entire Habitate application.
 *
 * All feature toggles live here — both build-level flags (e.g. certificate pinning)
 * and runtime feature flags (e.g. chat, stories).
 *
 * To connect to Firebase Remote Config in the future, inject FirebaseRemoteConfig
 * into [FeatureFlagsImpl] and read values with local defaults as fallback.
 */
interface FeatureFlags {
    // ── Build-level flags ──────────────────────────────────────────────
    /** Certificate pinning is enabled only for release builds. */
    val isCertificatePinningEnabled: Boolean

    // ── Feature toggles ────────────────────────────────────────────────
    val isChatEnabled: Boolean
    val isStoriesEnabled: Boolean
    val isFocusModeEnabled: Boolean
    val isAiPlannerEnabled: Boolean
}

@Singleton
class FeatureFlagsImpl @Inject constructor() : FeatureFlags {

    // Build-level: pinning is always ON in release, OFF in debug for proxy support
    override val isCertificatePinningEnabled: Boolean = !BuildConfig.DEBUG

    // Runtime feature toggles (local defaults — wire to Remote Config later)
    override val isChatEnabled: Boolean = false
    override val isStoriesEnabled: Boolean = false
    override val isFocusModeEnabled: Boolean = true
    override val isAiPlannerEnabled: Boolean = true
}
