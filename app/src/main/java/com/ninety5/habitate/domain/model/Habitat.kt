package com.ninety5.habitate.domain.model

import java.time.Instant

/**
 * Domain model for a Habitat (community/group).
 */
data class Habitat(
    val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val coverUrl: String?,
    val privacy: HabitatPrivacy,
    val memberCount: Int,
    val creatorId: String,
    val createdAt: Instant
)

data class HabitatMembership(
    val habitatId: String,
    val userId: String,
    val role: HabitatRole,
    val joinedAt: Instant? = null
)

enum class HabitatPrivacy {
    PUBLIC, PRIVATE, INVITE_ONLY
}

enum class HabitatRole {
    OWNER, ADMIN, MODERATOR, MEMBER
}
