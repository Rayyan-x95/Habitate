package com.ninety5.habitate.core

import com.ninety5.habitate.BuildConfig

object FeatureFlags {
    /**
     * Certificate pinning is enabled only for release builds to prevent
     * man-in-the-middle attacks.
     *
     * Debug builds remain unpinned to allow for proxying and debugging.
     */
    val CERTIFICATE_PINNING_ENABLED = !BuildConfig.DEBUG
}
