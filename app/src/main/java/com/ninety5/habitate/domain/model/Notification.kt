package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a notification.
 */
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val targetId: String?,
    val isRead: Boolean,
    val isDigest: Boolean,
    val createdAt: Instant
)

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, MENTION, CHALLENGE,
    HABITAT_INVITE, SYSTEM, STREAK_REMINDER, DIGEST
}
