package com.ninety5.habitate.core.analytics

import java.time.Instant

data class AnalyticsEvent(
    val name: String,
    val userId: String? = null,
    val deviceId: String? = null,
    val sessionId: String? = null,
    val timestamp: Instant = Instant.now(),
    val properties: Map<String, Any> = emptyMap()
)
