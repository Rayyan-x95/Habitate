package com.ninety5.habitate.domain.mapper

import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.NotificationEntity
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.remote.dto.PostDto
import com.ninety5.habitate.data.remote.dto.UserDto
import com.ninety5.habitate.data.remote.dto.NotificationDto
import com.ninety5.habitate.data.remote.dto.CommentDto

fun PostDto.toPostEntity(): PostEntity {
    val mediaList = if (mediaUris.isBlank() || mediaUris == "[]") {
        emptyList()
    } else {
        try {
            mediaUris.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    return PostEntity(
        id = id,
        authorId = author.id,
        contentText = contentText,
        mediaUrls = mediaList,
        visibility = try { Visibility.valueOf(visibility.uppercase()) } catch (e: Exception) { Visibility.PUBLIC },
        habitatId = habitatId,
        workoutId = workoutId,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isLiked = isLiked,
        syncState = SyncState.SYNCED,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}

fun UserDto.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        displayName = displayName,
        username = username,
        email = email,
        avatarUrl = avatarUrl,
        bio = null, // UserDto doesn't have bio currently
        createdAt = System.currentTimeMillis(), // Set current time for new users
        followerCount = 0, // Will be updated from server
        followingCount = 0, // Will be updated from server
        postCount = 0, // Will be updated from server
        isStealthMode = isStealthMode
    )
}

fun NotificationDto.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type,
        targetId = targetId,
        isRead = isRead,
        createdAt = createdAt
    )
}

fun CommentDto.toCommentEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        userId = userId,
        postId = postId,
        text = text,
        createdAt = createdAt.toEpochMilli(),
        syncState = SyncState.SYNCED
    )
}
