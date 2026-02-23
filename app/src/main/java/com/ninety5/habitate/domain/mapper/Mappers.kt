package com.ninety5.habitate.domain.mapper

import com.ninety5.habitate.data.local.entity.ChallengeEntity
import com.ninety5.habitate.data.local.entity.ChallengeProgressEntity
import com.ninety5.habitate.data.local.entity.FocusSessionEntity
import com.ninety5.habitate.data.local.entity.FocusSessionStatus
import com.ninety5.habitate.data.local.entity.InsightEntity
import com.ninety5.habitate.data.local.entity.NotificationEntity
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.TaskEntity
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.HabitEntity
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitStreakEntity
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.HabitatMembershipEntity
import com.ninety5.habitate.data.local.entity.JournalEntryEntity
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.local.entity.HabitatPrivacy as EntityHabitatPrivacy
import com.ninety5.habitate.data.local.entity.HabitatRole as EntityHabitatRole
import com.ninety5.habitate.data.local.entity.MessageReactionEntity
import com.ninety5.habitate.data.local.relation.HabitWithLogs
import com.ninety5.habitate.data.local.relation.HabitWithStreak
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import com.ninety5.habitate.data.local.relation.StoryWithUser
import com.ninety5.habitate.data.remote.dto.ChatDto
import com.ninety5.habitate.data.remote.dto.HabitatDto
import com.ninety5.habitate.data.remote.dto.JournalEntryDto
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.ninety5.habitate.data.remote.dto.PostDto
import com.ninety5.habitate.data.remote.dto.StoryDto
import com.ninety5.habitate.data.remote.dto.UserDto
import com.ninety5.habitate.data.remote.dto.NotificationDto
import com.ninety5.habitate.data.remote.dto.CommentDto
import com.ninety5.habitate.domain.model.Challenge
import com.ninety5.habitate.domain.model.ChallengeMetric
import com.ninety5.habitate.domain.model.ChallengeProgress
import com.ninety5.habitate.domain.model.ChallengeStatus
import com.ninety5.habitate.domain.model.FocusSession
import com.ninety5.habitate.domain.model.FocusStatus
import com.ninety5.habitate.domain.model.Habit
import com.ninety5.habitate.domain.model.HabitCategory
import com.ninety5.habitate.domain.model.HabitFrequency
import com.ninety5.habitate.domain.model.HabitLog
import com.ninety5.habitate.domain.model.HabitMood
import com.ninety5.habitate.domain.model.HabitStreak
import com.ninety5.habitate.domain.model.HabitWithDetails
import com.ninety5.habitate.domain.model.Insight
import com.ninety5.habitate.domain.model.InsightPriority
import com.ninety5.habitate.domain.model.InsightType
import com.ninety5.habitate.domain.model.Notification
import com.ninety5.habitate.domain.model.NotificationType
import com.ninety5.habitate.domain.model.Post
import com.ninety5.habitate.domain.model.PostVisibility
import com.ninety5.habitate.domain.model.Task
import com.ninety5.habitate.domain.model.TaskPriority
import com.ninety5.habitate.domain.model.TaskStatus
import com.ninety5.habitate.domain.model.User
import com.ninety5.habitate.domain.model.Workout
import com.ninety5.habitate.domain.model.WorkoutSource
import com.ninety5.habitate.domain.model.WorkoutType
import com.ninety5.habitate.domain.model.Conversation
import com.ninety5.habitate.domain.model.Habitat
import com.ninety5.habitate.domain.model.HabitatMembership
import com.ninety5.habitate.domain.model.HabitatPrivacy
import com.ninety5.habitate.domain.model.HabitatRole
import com.ninety5.habitate.domain.model.JournalEntry
import com.ninety5.habitate.domain.model.JournalMood
import com.ninety5.habitate.domain.model.Message
import com.ninety5.habitate.domain.model.MessageReaction
import com.ninety5.habitate.domain.model.Story
import com.ninety5.habitate.domain.model.DailySummary
import com.ninety5.habitate.domain.model.Comment
import com.ninety5.habitate.domain.model.Weather
import com.ninety5.habitate.domain.model.Quote
import com.ninety5.habitate.domain.model.Meal
import com.ninety5.habitate.domain.model.Book
import com.ninety5.habitate.data.local.dao.CommentWithUser
import com.ninety5.habitate.data.local.entity.DailySummaryEntity
import com.ninety5.habitate.data.remote.publicapis.BookDto
import com.ninety5.habitate.data.remote.publicapis.MealDto
import com.ninety5.habitate.data.remote.publicapis.QuoteDto
import com.ninety5.habitate.data.remote.publicapis.WeatherResponse
import timber.log.Timber
import java.time.Instant

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
    val parsedCreatedAt = try {
        java.time.Instant.parse(createdAt).toEpochMilli()
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse createdAt: $createdAt")
        System.currentTimeMillis()
    }
    return UserEntity(
        id = id,
        displayName = displayName,
        username = username,
        email = email,
        avatarUrl = avatarUrl,
        bio = bio,
        createdAt = parsedCreatedAt,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
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

// ═══════════════════════════════════════════════════════════════════════════
// USER ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        displayName = displayName,
        username = username,
        avatarUrl = avatarUrl,
        bio = bio,
        email = email,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        isOnline = isOnline,
        isStealthMode = isStealthMode,
        lastActive = lastActive,
        createdAt = createdAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        displayName = displayName,
        username = username,
        avatarUrl = avatarUrl,
        bio = bio,
        email = email,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        isOnline = isOnline,
        isStealthMode = isStealthMode,
        lastActive = lastActive,
        createdAt = createdAt
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// POST ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Convert a [PostEntity] + optional author [UserEntity] to a domain [Post].
 * The author fields are flattened into the Post model.
 */
fun PostEntity.toDomain(author: UserEntity? = null): Post {
    return Post(
        id = id,
        authorId = authorId,
        authorName = author?.displayName ?: "",
        authorUsername = author?.username ?: "",
        authorAvatarUrl = author?.avatarUrl,
        contentText = contentText ?: "",
        mediaUrls = mediaUrls,
        visibility = visibility.toPostVisibility(),
        habitatId = habitatId,
        workoutId = workoutId,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isLiked = isLiked,
        isArchived = isArchived,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun Post.toEntity(syncState: SyncState = SyncState.SYNCED): PostEntity {
    return PostEntity(
        id = id,
        authorId = authorId,
        contentText = contentText,
        mediaUrls = mediaUrls,
        visibility = visibility.toEntityVisibility(),
        habitatId = habitatId,
        workoutId = workoutId,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isLiked = isLiked,
        isArchived = isArchived,
        syncState = syncState,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli()
    )
}

// ── Visibility enum mappers ────────────────────────────────────────────────

fun Visibility.toPostVisibility(): PostVisibility = when (this) {
    Visibility.PUBLIC -> PostVisibility.PUBLIC
    Visibility.PRIVATE -> PostVisibility.PRIVATE
    Visibility.FRIENDS -> PostVisibility.FOLLOWERS_ONLY
    Visibility.HABITAT_ONLY -> PostVisibility.HABITAT_ONLY
}

fun PostVisibility.toEntityVisibility(): Visibility = when (this) {
    PostVisibility.PUBLIC -> Visibility.PUBLIC
    PostVisibility.PRIVATE -> Visibility.PRIVATE
    PostVisibility.FOLLOWERS_ONLY -> Visibility.FRIENDS
    PostVisibility.HABITAT_ONLY -> Visibility.HABITAT_ONLY
}

// ═══════════════════════════════════════════════════════════════════════════
// HABIT ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = category.toDomainCategory(),
        color = color,
        icon = icon,
        frequency = frequency.toDomainFrequency(),
        customSchedule = customSchedule ?: emptyList(),
        reminderTime = reminderTime,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Habit.toEntity(syncState: SyncState = SyncState.SYNCED): HabitEntity {
    return HabitEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = category.toEntityCategory(),
        color = color,
        icon = icon,
        frequency = frequency.toEntityFrequency(),
        customSchedule = customSchedule.ifEmpty { null },
        reminderTime = reminderTime,
        reminderEnabled = reminderTime != null,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState
    )
}

fun HabitLogEntity.toDomain(): HabitLog {
    return HabitLog(
        id = id,
        habitId = habitId,
        completedAt = completedAt,
        mood = mood?.toDomainMood(),
        note = note
    )
}

fun HabitStreakEntity.toDomain(): HabitStreak {
    return HabitStreak(
        habitId = habitId,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalCompletions = totalCompletions,
        lastCompletedAt = lastCompletedDate?.let {
            try { java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneOffset.UTC).toInstant() }
            catch (_: Exception) { null }
        }
    )
}

/**
 * Map Room [HabitWithLogs] relation to domain [HabitWithDetails].
 * Streak is loaded separately, so [streak] param is optional.
 */
fun HabitWithLogs.toDomain(streak: HabitStreakEntity? = null): HabitWithDetails {
    return HabitWithDetails(
        habit = habit.toDomain(),
        streak = streak?.toDomain() ?: HabitStreak(
            habitId = habit.id,
            currentStreak = 0,
            longestStreak = 0,
            totalCompletions = 0,
            lastCompletedAt = null
        ),
        recentLogs = logs.map { it.toDomain() }
    )
}

/**
 * Map Room [HabitWithStreak] relation to domain [HabitWithDetails].
 * Logs are not available, so [recentLogs] is empty.
 */
fun HabitWithStreak.toDomain(): HabitWithDetails {
    return HabitWithDetails(
        habit = habit.toDomain(),
        streak = streak?.toDomain() ?: HabitStreak(
            habitId = habit.id,
            currentStreak = 0,
            longestStreak = 0,
            totalCompletions = 0,
            lastCompletedAt = null
        ),
        recentLogs = emptyList()
    )
}

// ── Habit enum mappers ────────────────────────────────────────────────────

fun com.ninety5.habitate.data.local.entity.HabitCategory.toDomainCategory(): HabitCategory = when (this) {
    com.ninety5.habitate.data.local.entity.HabitCategory.HEALTH -> HabitCategory.HEALTH
    com.ninety5.habitate.data.local.entity.HabitCategory.FITNESS -> HabitCategory.FITNESS
    com.ninety5.habitate.data.local.entity.HabitCategory.MINDFULNESS -> HabitCategory.MINDFULNESS
    com.ninety5.habitate.data.local.entity.HabitCategory.PRODUCTIVITY -> HabitCategory.PRODUCTIVITY
    com.ninety5.habitate.data.local.entity.HabitCategory.LEARNING -> HabitCategory.LEARNING
    com.ninety5.habitate.data.local.entity.HabitCategory.SOCIAL -> HabitCategory.SOCIAL
    com.ninety5.habitate.data.local.entity.HabitCategory.CREATIVITY -> HabitCategory.CREATIVITY
    com.ninety5.habitate.data.local.entity.HabitCategory.FINANCE -> HabitCategory.FINANCE
    com.ninety5.habitate.data.local.entity.HabitCategory.OTHER -> HabitCategory.CUSTOM
}

fun HabitCategory.toEntityCategory(): com.ninety5.habitate.data.local.entity.HabitCategory = when (this) {
    HabitCategory.HEALTH -> com.ninety5.habitate.data.local.entity.HabitCategory.HEALTH
    HabitCategory.FITNESS -> com.ninety5.habitate.data.local.entity.HabitCategory.FITNESS
    HabitCategory.MINDFULNESS -> com.ninety5.habitate.data.local.entity.HabitCategory.MINDFULNESS
    HabitCategory.PRODUCTIVITY -> com.ninety5.habitate.data.local.entity.HabitCategory.PRODUCTIVITY
    HabitCategory.LEARNING -> com.ninety5.habitate.data.local.entity.HabitCategory.LEARNING
    HabitCategory.SOCIAL -> com.ninety5.habitate.data.local.entity.HabitCategory.SOCIAL
    HabitCategory.CREATIVITY -> com.ninety5.habitate.data.local.entity.HabitCategory.CREATIVITY
    HabitCategory.FINANCE -> com.ninety5.habitate.data.local.entity.HabitCategory.FINANCE
    HabitCategory.CUSTOM -> com.ninety5.habitate.data.local.entity.HabitCategory.OTHER
}

fun com.ninety5.habitate.data.local.entity.HabitFrequency.toDomainFrequency(): HabitFrequency = when (this) {
    com.ninety5.habitate.data.local.entity.HabitFrequency.DAILY -> HabitFrequency.DAILY
    com.ninety5.habitate.data.local.entity.HabitFrequency.WEEKLY -> HabitFrequency.WEEKLY
    com.ninety5.habitate.data.local.entity.HabitFrequency.CUSTOM -> HabitFrequency.CUSTOM
}

fun HabitFrequency.toEntityFrequency(): com.ninety5.habitate.data.local.entity.HabitFrequency = when (this) {
    HabitFrequency.DAILY -> com.ninety5.habitate.data.local.entity.HabitFrequency.DAILY
    HabitFrequency.WEEKLY -> com.ninety5.habitate.data.local.entity.HabitFrequency.WEEKLY
    HabitFrequency.CUSTOM -> com.ninety5.habitate.data.local.entity.HabitFrequency.CUSTOM
}

fun com.ninety5.habitate.data.local.entity.HabitMood.toDomainMood(): HabitMood = when (this) {
    com.ninety5.habitate.data.local.entity.HabitMood.GREAT -> HabitMood.GREAT
    com.ninety5.habitate.data.local.entity.HabitMood.GOOD -> HabitMood.GOOD
    com.ninety5.habitate.data.local.entity.HabitMood.OKAY -> HabitMood.NEUTRAL
    com.ninety5.habitate.data.local.entity.HabitMood.HARD -> HabitMood.BAD
    com.ninety5.habitate.data.local.entity.HabitMood.TERRIBLE -> HabitMood.TERRIBLE
}

fun HabitMood.toEntityMood(): com.ninety5.habitate.data.local.entity.HabitMood = when (this) {
    HabitMood.GREAT -> com.ninety5.habitate.data.local.entity.HabitMood.GREAT
    HabitMood.GOOD -> com.ninety5.habitate.data.local.entity.HabitMood.GOOD
    HabitMood.NEUTRAL -> com.ninety5.habitate.data.local.entity.HabitMood.OKAY
    HabitMood.BAD -> com.ninety5.habitate.data.local.entity.HabitMood.HARD
    HabitMood.TERRIBLE -> com.ninety5.habitate.data.local.entity.HabitMood.TERRIBLE
}

// ═══════════════════════════════════════════════════════════════════════════
// TASK ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        userId = "", // Entity doesn't store userId — filled by repo
        title = title,
        description = description,
        priority = priority.toDomainPriority(),
        status = status.toDomainStatus(),
        dueAt = dueAt,
        recurrenceRule = recurrenceRule,
        categoryId = null,
        linkedEntityId = linkedEntityId,
        linkedEntityType = linkedEntityType,
        isArchived = status == com.ninety5.habitate.data.local.entity.TaskStatus.ARCHIVED,
        createdAt = updatedAt, // Entity lacks createdAt; use updatedAt as best-effort
        updatedAt = updatedAt
    )
}

fun Task.toEntity(syncState: SyncState = SyncState.SYNCED): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        dueAt = dueAt,
        recurrenceRule = recurrenceRule,
        priority = priority.toEntityPriority(),
        status = status.toEntityStatus(),
        linkedEntityId = linkedEntityId,
        linkedEntityType = linkedEntityType,
        syncState = syncState,
        updatedAt = updatedAt
    )
}

// ── Task enum mappers ─────────────────────────────────────────────────────

fun com.ninety5.habitate.data.local.entity.TaskPriority.toDomainPriority(): TaskPriority = when (this) {
    com.ninety5.habitate.data.local.entity.TaskPriority.LOW -> TaskPriority.LOW
    com.ninety5.habitate.data.local.entity.TaskPriority.MEDIUM -> TaskPriority.MEDIUM
    com.ninety5.habitate.data.local.entity.TaskPriority.HIGH -> TaskPriority.HIGH
    com.ninety5.habitate.data.local.entity.TaskPriority.URGENT -> TaskPriority.URGENT
}

fun TaskPriority.toEntityPriority(): com.ninety5.habitate.data.local.entity.TaskPriority = when (this) {
    TaskPriority.LOW -> com.ninety5.habitate.data.local.entity.TaskPriority.LOW
    TaskPriority.MEDIUM -> com.ninety5.habitate.data.local.entity.TaskPriority.MEDIUM
    TaskPriority.HIGH -> com.ninety5.habitate.data.local.entity.TaskPriority.HIGH
    TaskPriority.URGENT -> com.ninety5.habitate.data.local.entity.TaskPriority.URGENT
}

fun com.ninety5.habitate.data.local.entity.TaskStatus.toDomainStatus(): TaskStatus = when (this) {
    com.ninety5.habitate.data.local.entity.TaskStatus.OPEN -> TaskStatus.PENDING
    com.ninety5.habitate.data.local.entity.TaskStatus.IN_PROGRESS -> TaskStatus.IN_PROGRESS
    com.ninety5.habitate.data.local.entity.TaskStatus.DONE -> TaskStatus.COMPLETED
    com.ninety5.habitate.data.local.entity.TaskStatus.ARCHIVED -> TaskStatus.CANCELLED
}

fun TaskStatus.toEntityStatus(): com.ninety5.habitate.data.local.entity.TaskStatus = when (this) {
    TaskStatus.PENDING -> com.ninety5.habitate.data.local.entity.TaskStatus.OPEN
    TaskStatus.IN_PROGRESS -> com.ninety5.habitate.data.local.entity.TaskStatus.IN_PROGRESS
    TaskStatus.COMPLETED -> com.ninety5.habitate.data.local.entity.TaskStatus.DONE
    TaskStatus.CANCELLED -> com.ninety5.habitate.data.local.entity.TaskStatus.ARCHIVED
}

// ═══════════════════════════════════════════════════════════════════════════
// INSIGHT ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun InsightEntity.toDomain(): Insight {
    return Insight(
        id = id,
        type = type.toDomainInsightType(),
        title = title,
        description = description,
        priority = priority.toDomainInsightPriority(),
        actionLabel = null, // Entity doesn't store these
        actionRoute = null,
        isDismissed = isDismissed,
        createdAt = createdAt
    )
}

fun Insight.toEntity(): InsightEntity {
    return InsightEntity(
        id = id,
        type = type.toEntityInsightType(),
        title = title,
        description = description,
        priority = priority.toEntityInsightPriority(),
        relatedEntityId = null,
        createdAt = createdAt,
        isDismissed = isDismissed
    )
}

fun com.ninety5.habitate.data.local.entity.InsightType.toDomainInsightType(): InsightType = when (this) {
    com.ninety5.habitate.data.local.entity.InsightType.STREAK_RISK -> InsightType.STREAK_RISK
    com.ninety5.habitate.data.local.entity.InsightType.MILESTONE_APPROACHING -> InsightType.MILESTONE
    com.ninety5.habitate.data.local.entity.InsightType.WEEKLY_SUMMARY -> InsightType.WEEKLY_SUMMARY
    com.ninety5.habitate.data.local.entity.InsightType.SUGGESTION -> InsightType.HABIT_SUGGESTION
    com.ninety5.habitate.data.local.entity.InsightType.PATTERN_DETECTED -> InsightType.PATTERN_DETECTED
    com.ninety5.habitate.data.local.entity.InsightType.MOOD_CORRELATION -> InsightType.MOOD_CORRELATION
    com.ninety5.habitate.data.local.entity.InsightType.ENERGY_TREND -> InsightType.ENERGY_TREND
    com.ninety5.habitate.data.local.entity.InsightType.TASK_FAILURE -> InsightType.TASK_FAILURE
    com.ninety5.habitate.data.local.entity.InsightType.HABIT_FRICTION -> InsightType.HABIT_FRICTION
}

fun InsightType.toEntityInsightType(): com.ninety5.habitate.data.local.entity.InsightType = when (this) {
    InsightType.STREAK_RISK -> com.ninety5.habitate.data.local.entity.InsightType.STREAK_RISK
    InsightType.MILESTONE -> com.ninety5.habitate.data.local.entity.InsightType.MILESTONE_APPROACHING
    InsightType.WEEKLY_SUMMARY -> com.ninety5.habitate.data.local.entity.InsightType.WEEKLY_SUMMARY
    InsightType.HABIT_SUGGESTION -> com.ninety5.habitate.data.local.entity.InsightType.SUGGESTION
    InsightType.INACTIVITY_ALERT -> com.ninety5.habitate.data.local.entity.InsightType.TASK_FAILURE
    InsightType.PRODUCTIVITY_TIP -> com.ninety5.habitate.data.local.entity.InsightType.PATTERN_DETECTED
    InsightType.PATTERN_DETECTED -> com.ninety5.habitate.data.local.entity.InsightType.PATTERN_DETECTED
    InsightType.MOOD_CORRELATION -> com.ninety5.habitate.data.local.entity.InsightType.MOOD_CORRELATION
    InsightType.ENERGY_TREND -> com.ninety5.habitate.data.local.entity.InsightType.ENERGY_TREND
    InsightType.TASK_FAILURE -> com.ninety5.habitate.data.local.entity.InsightType.TASK_FAILURE
    InsightType.HABIT_FRICTION -> com.ninety5.habitate.data.local.entity.InsightType.HABIT_FRICTION
}

fun com.ninety5.habitate.data.local.entity.InsightPriority.toDomainInsightPriority(): InsightPriority = when (this) {
    com.ninety5.habitate.data.local.entity.InsightPriority.LOW -> InsightPriority.LOW
    com.ninety5.habitate.data.local.entity.InsightPriority.MEDIUM -> InsightPriority.MEDIUM
    com.ninety5.habitate.data.local.entity.InsightPriority.HIGH -> InsightPriority.HIGH
}

fun InsightPriority.toEntityInsightPriority(): com.ninety5.habitate.data.local.entity.InsightPriority = when (this) {
    InsightPriority.LOW -> com.ninety5.habitate.data.local.entity.InsightPriority.LOW
    InsightPriority.MEDIUM -> com.ninety5.habitate.data.local.entity.InsightPriority.MEDIUM
    InsightPriority.HIGH -> com.ninety5.habitate.data.local.entity.InsightPriority.HIGH
    InsightPriority.CRITICAL -> com.ninety5.habitate.data.local.entity.InsightPriority.HIGH // No CRITICAL in entity
}

// ═══════════════════════════════════════════════════════════════════════════
// NOTIFICATION ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = try {
            NotificationType.valueOf(type.uppercase())
        } catch (_: Exception) {
            NotificationType.SYSTEM
        },
        targetId = targetId,
        isRead = isRead,
        isDigest = isDigest,
        createdAt = createdAt
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type.name,
        targetId = targetId,
        isRead = isRead,
        isDigest = isDigest,
        createdAt = createdAt
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// CHALLENGE ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun ChallengeEntity.toDomain(isJoined: Boolean = false, participantCount: Int = 0): Challenge {
    return Challenge(
        id = id,
        title = title,
        description = description,
        habitatId = habitatId,
        creatorId = creatorId,
        metricType = try {
            ChallengeMetric.valueOf(metricType.uppercase())
        } catch (_: Exception) {
            ChallengeMetric.CUSTOM
        },
        targetValue = targetValue,
        startDate = startDate,
        endDate = endDate,
        participantCount = participantCount,
        isJoined = isJoined,
        createdAt = createdAt
    )
}

fun Challenge.toEntity(syncState: SyncState = SyncState.SYNCED): ChallengeEntity {
    return ChallengeEntity(
        id = id,
        title = title,
        description = description,
        metricType = metricType.name,
        targetValue = targetValue,
        startDate = startDate,
        endDate = endDate,
        creatorId = creatorId,
        habitatId = habitatId,
        syncState = syncState,
        createdAt = createdAt
    )
}

fun ChallengeProgressEntity.toDomain(): ChallengeProgress {
    return ChallengeProgress(
        challengeId = challengeId,
        userId = userId,
        currentValue = progress,
        status = status.toDomainChallengeStatus(),
        rank = null // Computed server-side
    )
}

fun com.ninety5.habitate.data.local.entity.ChallengeStatus.toDomainChallengeStatus(): ChallengeStatus = when (this) {
    com.ninety5.habitate.data.local.entity.ChallengeStatus.JOINED -> ChallengeStatus.ACTIVE
    com.ninety5.habitate.data.local.entity.ChallengeStatus.COMPLETED -> ChallengeStatus.COMPLETED
    com.ninety5.habitate.data.local.entity.ChallengeStatus.FAILED -> ChallengeStatus.FAILED
}

fun ChallengeStatus.toEntityChallengeStatus(): com.ninety5.habitate.data.local.entity.ChallengeStatus = when (this) {
    ChallengeStatus.ACTIVE -> com.ninety5.habitate.data.local.entity.ChallengeStatus.JOINED
    ChallengeStatus.COMPLETED -> com.ninety5.habitate.data.local.entity.ChallengeStatus.COMPLETED
    ChallengeStatus.FAILED -> com.ninety5.habitate.data.local.entity.ChallengeStatus.FAILED
    ChallengeStatus.WITHDRAWN -> com.ninety5.habitate.data.local.entity.ChallengeStatus.FAILED // No WITHDRAWN in entity
}

// ═══════════════════════════════════════════════════════════════════════════
// WORKOUT ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun WorkoutEntity.toDomain(): Workout {
    return Workout(
        id = id,
        userId = "", // Entity doesn't store userId — filled by repo
        type = try {
            WorkoutType.valueOf(type.uppercase())
        } catch (_: Exception) {
            WorkoutType.OTHER
        },
        source = source.toDomainSource(),
        externalId = externalId,
        startTime = startTs,
        endTime = endTs,
        durationSeconds = java.time.Duration.between(startTs, endTs).seconds.coerceAtLeast(0),
        distanceMeters = distanceMeters,
        caloriesBurned = calories?.toInt(),
        heartRateAvg = null, // Entity doesn't store this
        notes = null, // Entity doesn't store this
        isArchived = isArchived,
        createdAt = updatedAt // Entity lacks createdAt; use updatedAt
    )
}

fun Workout.toEntity(syncState: SyncState = SyncState.SYNCED): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        source = source.toEntitySource(),
        externalId = externalId,
        type = type.name,
        startTs = startTime,
        endTs = endTime ?: startTime.plusSeconds(durationSeconds),
        distanceMeters = distanceMeters,
        calories = caloriesBurned?.toDouble(),
        isArchived = isArchived,
        syncState = syncState,
        updatedAt = Instant.now() // Entity updatedAt tracks last modification time
    )
}

fun com.ninety5.habitate.data.local.entity.WorkoutSource.toDomainSource(): WorkoutSource = when (this) {
    com.ninety5.habitate.data.local.entity.WorkoutSource.MANUAL -> WorkoutSource.MANUAL
    com.ninety5.habitate.data.local.entity.WorkoutSource.HEALTH_CONNECT -> WorkoutSource.HEALTH_CONNECT
}

fun WorkoutSource.toEntitySource(): com.ninety5.habitate.data.local.entity.WorkoutSource = when (this) {
    WorkoutSource.MANUAL -> com.ninety5.habitate.data.local.entity.WorkoutSource.MANUAL
    WorkoutSource.HEALTH_CONNECT -> com.ninety5.habitate.data.local.entity.WorkoutSource.HEALTH_CONNECT
    WorkoutSource.IMPORTED -> com.ninety5.habitate.data.local.entity.WorkoutSource.MANUAL // No IMPORTED in entity
}

// ═══════════════════════════════════════════════════════════════════════════
// FOCUS SESSION ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun FocusSessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        status = status.toDomainFocusStatus(),
        soundTrack = soundTrack,
        rating = rating,
        notes = null, // Entity doesn't store notes
        createdAt = startTime // Entity lacks createdAt; use startTime
    )
}

fun FocusSession.toEntity(syncState: SyncState = SyncState.SYNCED): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        status = status.toEntityFocusStatus(),
        soundTrack = soundTrack,
        rating = rating,
        syncState = syncState,
        updatedAt = createdAt // Preserve source timestamp
    )
}

fun FocusSessionStatus.toDomainFocusStatus(): FocusStatus = when (this) {
    FocusSessionStatus.IN_PROGRESS -> FocusStatus.IN_PROGRESS
    FocusSessionStatus.PAUSED -> FocusStatus.PAUSED
    FocusSessionStatus.COMPLETED -> FocusStatus.COMPLETED
    FocusSessionStatus.ABORTED -> FocusStatus.CANCELLED
}

fun FocusStatus.toEntityFocusStatus(): FocusSessionStatus = when (this) {
    FocusStatus.IN_PROGRESS -> FocusSessionStatus.IN_PROGRESS
    FocusStatus.PAUSED -> FocusSessionStatus.PAUSED
    FocusStatus.COMPLETED -> FocusSessionStatus.COMPLETED
    FocusStatus.CANCELLED -> FocusSessionStatus.ABORTED
}

// ═══════════════════════════════════════════════════════════════════════════
// STORY ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun StoryEntity.toDomain(viewCount: Int = 0): Story {
    return Story(
        id = id,
        userId = userId,
        mediaUrl = mediaUrl,
        caption = caption,
        visibility = visibility.toPostVisibility(),
        viewCount = viewCount,
        isSaved = isSaved,
        createdAt = Instant.ofEpochMilli(createdAt),
        expiresAt = Instant.ofEpochMilli(expiresAt)
    )
}

fun Story.toEntity(syncState: SyncState = SyncState.SYNCED): StoryEntity {
    return StoryEntity(
        id = id,
        userId = userId,
        mediaUrl = mediaUrl,
        caption = caption,
        visibility = visibility.toEntityVisibility(),
        createdAt = createdAt.toEpochMilli(),
        expiresAt = expiresAt.toEpochMilli(),
        isSaved = isSaved,
        syncState = syncState
    )
}

fun StoryDto.toEntity(): StoryEntity {
    return StoryEntity(
        id = id,
        userId = authorId,
        mediaUrl = mediaUri,
        caption = caption,
        visibility = try {
            Visibility.valueOf(visibility.uppercase())
        } catch (_: Exception) {
            Visibility.PUBLIC
        },
        createdAt = createdAt.toEpochMilli(),
        expiresAt = expiresAt.toEpochMilli(),
        syncState = SyncState.SYNCED
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// JOURNAL ENTRY ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = id,
        userId = userId,
        title = title,
        content = content,
        mood = mood?.let {
            try { JournalMood.valueOf(it.uppercase()) } catch (_: Exception) { null }
        },
        tags = tags,
        mediaUrls = mediaUrls,
        isPrivate = isPrivate,
        createdAt = createdAt,
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun JournalEntry.toEntity(syncState: SyncState = SyncState.SYNCED): JournalEntryEntity {
    return JournalEntryEntity(
        id = id,
        userId = userId,
        title = title,
        content = content,
        mood = mood?.name,
        tags = tags,
        mediaUrls = mediaUrls,
        date = createdAt.toEpochMilli(),
        isPrivate = isPrivate,
        syncState = syncState,
        updatedAt = updatedAt.toEpochMilli(),
        createdAt = createdAt
    )
}

fun JournalEntryDto.toEntity(userId: String): JournalEntryEntity {
    return JournalEntryEntity(
        id = id,
        userId = userId,
        title = title,
        content = content,
        mood = mood,
        tags = emptyList(), // DTO tags is a JSON string; parsed by caller if needed
        mediaUrls = emptyList(),
        date = createdAt.toEpochMilli(),
        isPrivate = isPrivate,
        syncState = SyncState.SYNCED,
        updatedAt = updatedAt.toEpochMilli(),
        createdAt = createdAt
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// HABITAT ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun HabitatEntity.toDomain(): Habitat {
    return Habitat(
        id = id,
        name = name,
        description = description,
        avatarUrl = null, // Entity doesn't store avatar separately
        coverUrl = coverImageUrl,
        privacy = privacy.toDomainPrivacy(),
        memberCount = memberCount,
        creatorId = "", // Entity doesn't store creatorId
        createdAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun Habitat.toEntity(syncState: SyncState = SyncState.SYNCED): HabitatEntity {
    return HabitatEntity(
        id = id,
        name = name,
        description = description,
        coverImageUrl = coverUrl,
        memberCount = memberCount,
        privacy = privacy.toEntityPrivacy(),
        syncState = syncState,
        updatedAt = createdAt.toEpochMilli()
    )
}

fun HabitatDto.toEntity(): HabitatEntity {
    return HabitatEntity(
        id = id,
        name = name,
        description = description,
        coverImageUrl = avatarUrl,
        memberCount = 0,
        privacy = try {
            EntityHabitatPrivacy.valueOf(privacyLevel.uppercase())
        } catch (_: Exception) {
            EntityHabitatPrivacy.PUBLIC
        },
        syncState = SyncState.SYNCED,
        updatedAt = updatedAt.toEpochMilli()
    )
}

fun HabitatMembershipEntity.toDomain(): HabitatMembership {
    return HabitatMembership(
        habitatId = habitatId,
        userId = userId,
        role = role.toDomainRole(),
        joinedAt = null // Entity doesn't store joinedAt; populated from server when available
    )
}

fun HabitatMembership.toEntity(
    id: String = java.util.UUID.randomUUID().toString(),
    syncState: SyncState = SyncState.SYNCED
): HabitatMembershipEntity {
    return HabitatMembershipEntity(
        id = id,
        habitatId = habitatId,
        userId = userId,
        role = role.toEntityRole(),
        syncState = syncState
    )
}

// ── HabitatPrivacy enum mappers ────────────────────────────────────────

fun EntityHabitatPrivacy.toDomainPrivacy(): HabitatPrivacy = when (this) {
    EntityHabitatPrivacy.PUBLIC -> HabitatPrivacy.PUBLIC
    EntityHabitatPrivacy.PRIVATE -> HabitatPrivacy.PRIVATE
    EntityHabitatPrivacy.SECRET -> HabitatPrivacy.INVITE_ONLY
}

fun HabitatPrivacy.toEntityPrivacy(): EntityHabitatPrivacy = when (this) {
    HabitatPrivacy.PUBLIC -> EntityHabitatPrivacy.PUBLIC
    HabitatPrivacy.PRIVATE -> EntityHabitatPrivacy.PRIVATE
    HabitatPrivacy.INVITE_ONLY -> EntityHabitatPrivacy.SECRET
}

// ── HabitatRole enum mappers ───────────────────────────────────────────

fun EntityHabitatRole.toDomainRole(): HabitatRole = when (this) {
    EntityHabitatRole.OWNER -> HabitatRole.OWNER
    EntityHabitatRole.ADMIN -> HabitatRole.ADMIN
    EntityHabitatRole.MODERATOR -> HabitatRole.MODERATOR
    EntityHabitatRole.MEMBER -> HabitatRole.MEMBER
}

fun HabitatRole.toEntityRole(): EntityHabitatRole = when (this) {
    HabitatRole.OWNER -> EntityHabitatRole.OWNER
    HabitatRole.ADMIN -> EntityHabitatRole.ADMIN
    HabitatRole.MODERATOR -> EntityHabitatRole.MODERATOR
    HabitatRole.MEMBER -> EntityHabitatRole.MEMBER
}

// ═══════════════════════════════════════════════════════════════════════════
// CHAT / MESSAGE ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun ChatEntity.toDomain(): Conversation {
    return Conversation(
        id = id,
        name = title,
        avatarUrl = null,
        isGroup = type == com.ninety5.habitate.data.local.entity.ChatType.HABITAT,
        isMuted = isMuted,
        lastMessageText = lastMessage,
        lastMessageAt = updatedAt.let { if (it > 0) Instant.ofEpochMilli(it) else null },
        participants = emptyList(),
        lastMessage = null,
        unreadCount = 0,
        createdAt = Instant.ofEpochMilli(updatedAt)
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        conversationId = chatId,
        senderId = senderId,
        content = content ?: "",
        mediaUrl = mediaUrl,
        timestamp = Instant.ofEpochMilli(createdAt),
        isRead = status == MessageStatus.READ,
        isDeleted = isDeleted
    )
}

fun Message.toEntity(status: MessageStatus = MessageStatus.SENDING): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = conversationId,
        senderId = senderId,
        content = content,
        mediaUrl = mediaUrl,
        status = if (isRead) MessageStatus.READ else status,
        createdAt = timestamp.toEpochMilli(),
        isDeleted = isDeleted
    )
}

fun MessageWithReactions.toDomain(): Message {
    return Message(
        id = message.id,
        conversationId = message.chatId,
        senderId = message.senderId,
        content = message.content ?: "",
        mediaUrl = message.mediaUrl,
        timestamp = Instant.ofEpochMilli(message.createdAt),
        isRead = message.status == MessageStatus.READ,
        isDeleted = message.isDeleted,
        reactions = reactions.map { MessageReaction(userId = it.userId, emoji = it.emoji) }
    )
}

fun StoryWithUser.toDomain(viewCount: Int = 0): Story {
    return Story(
        id = story.id,
        userId = story.userId,
        authorName = user?.displayName ?: "",
        authorAvatarUrl = user?.avatarUrl,
        mediaUrl = story.mediaUrl,
        caption = story.caption,
        visibility = story.visibility.toPostVisibility(),
        viewCount = viewCount,
        isSaved = story.isSaved,
        createdAt = Instant.ofEpochMilli(story.createdAt),
        expiresAt = Instant.ofEpochMilli(story.expiresAt)
    )
}

fun ChatDto.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        type = type,
        title = title,
        lastMessage = lastMessage,
        updatedAt = updatedAt
    )
}

fun MessageDto.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        mediaUrl = mediaUrl,
        status = status,
        createdAt = createdAt
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// DAILY SUMMARY ENTITY ↔ DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun DailySummaryEntity.toDomain(): DailySummary {
    return DailySummary(
        date = date,
        steps = steps,
        caloriesBurned = caloriesBurned,
        distanceMeters = distanceMeters,
        activeMinutes = activeMinutes,
        mood = mood,
        notes = notes
    )
}

fun DailySummary.toEntity(): DailySummaryEntity {
    return DailySummaryEntity(
        date = date,
        steps = steps,
        caloriesBurned = caloriesBurned,
        distanceMeters = distanceMeters,
        activeMinutes = activeMinutes,
        mood = mood,
        notes = notes
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// COMMENT WITH USER → DOMAIN COMMENT MAPPER
// ═══════════════════════════════════════════════════════════════════════════

fun CommentWithUser.toDomain(): Comment {
    return Comment(
        id = comment.id,
        userId = comment.userId,
        postId = comment.postId,
        text = comment.text,
        authorDisplayName = user.displayName,
        authorAvatarUrl = user.avatarUrl,
        createdAt = Instant.ofEpochMilli(comment.createdAt)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// TIMELINE VIEW → DOMAIN TIMELINE ITEM MAPPER
// ═══════════════════════════════════════════════════════════════════════════

fun com.ninety5.habitate.data.local.view.TimelineItem.toDomain(): com.ninety5.habitate.domain.model.TimelineItem {
    return com.ninety5.habitate.domain.model.TimelineItem(
        id = id,
        type = type,
        timestamp = timestamp,
        title = title,
        subtitle = subtitle,
        isArchived = isArchived
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// PUBLIC API DTO → DOMAIN MAPPERS
// ═══════════════════════════════════════════════════════════════════════════

fun WeatherResponse.toDomain(): Weather {
    return Weather(
        temperature = currentWeather.temperature,
        windSpeed = currentWeather.windSpeed,
        windDirection = currentWeather.windDirection,
        weatherCode = currentWeather.weatherCode,
        time = currentWeather.time
    )
}

fun QuoteDto.toDomain(): Quote {
    return Quote(
        text = quote,
        author = author
    )
}

fun MealDto.toDomain(): Meal {
    return Meal(
        id = id,
        name = name,
        category = category,
        area = area,
        instructions = instructions,
        thumbnailUrl = thumbUrl
    )
}

fun BookDto.toDomain(): Book {
    return Book(
        key = key,
        title = title,
        authors = authorName ?: emptyList(),
        firstPublishYear = firstPublishYear,
        coverId = coverId
    )
}
