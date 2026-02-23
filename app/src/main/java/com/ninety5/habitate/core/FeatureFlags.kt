package com.ninety5.habitate.core

import com.ninety5.habitate.BuildConfig

/**
 * @deprecated Use [com.ninety5.habitate.util.FeatureFlags] interface instead.
 * This object is kept only for backward compatibility in non-DI contexts
 * (e.g., Hilt module providers that cannot inject interfaces).
 * All new code should inject [com.ninety5.habitate.util.FeatureFlags] via Hilt.
 */
@Deprecated(
    message = "Use the Hilt-injectable FeatureFlags interface from util package",
    replaceWith = ReplaceWith(
        "FeatureFlags",
        "com.ninety5.habitate.util.FeatureFlags"
    )
)
object CoreFeatureFlags {
    val CERTIFICATE_PINNING_ENABLED = !BuildConfig.DEBUG
}
