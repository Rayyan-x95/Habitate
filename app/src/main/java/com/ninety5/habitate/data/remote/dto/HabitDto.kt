package com.ninety5.habitate.data.remote.dto

import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.local.entity.HabitEntity
import com.ninety5.habitate.data.local.entity.HabitFrequency
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitMood
import com.ninety5.habitate.data.local.entity.SyncState
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Data Transfer Object for habits.
 * Used for API communication with the backend.
 */
@JsonClass(generateAdapter = true)
data class HabitDto(
    @Json(name = "id") val id: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "category") val category: String,
    @Json(name = "color") val color: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "frequency") val frequency: String,
    @Json(name = "customSchedule") val customSchedule: List<String>? = null, // Days of week as strings
    @Json(name = "reminderTime") val reminderTime: String? = null, // ISO time format
    @Json(name = "reminderEnabled") val reminderEnabled: Boolean = false,
    @Json(name = "isArchived") val isArchived: Boolean = false,
    @Json(name = "createdAt") val createdAt: Instant,
    @Json(name = "updatedAt") val updatedAt: Instant
) {
    fun toEntity(): HabitEntity {
        return HabitEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            category = try { HabitCategory.valueOf(category) } catch (e: Exception) { HabitCategory.OTHER },
            color = color,
            icon = icon,
            frequency = try { HabitFrequency.valueOf(frequency) } catch (e: Exception) { HabitFrequency.DAILY },
            customSchedule = customSchedule?.mapNotNull { 
                try { DayOfWeek.valueOf(it) } catch (e: Exception) { null }
            },
            reminderTime = reminderTime?.let { 
                try { LocalTime.parse(it) } catch (e: Exception) { null }
            },
            reminderEnabled = reminderEnabled,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncState = SyncState.SYNCED
        )
    }

    companion object {
        fun fromEntity(entity: HabitEntity): HabitDto {
            return HabitDto(
                id = entity.id,
                userId = entity.userId,
                title = entity.title,
                description = entity.description,
                category = entity.category.name,
                color = entity.color,
                icon = entity.icon,
                frequency = entity.frequency.name,
                customSchedule = entity.customSchedule?.map { it.name },
                reminderTime = entity.reminderTime?.toString(),
                reminderEnabled = entity.reminderEnabled,
                isArchived = entity.isArchived,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}

/**
 * Data Transfer Object for habit logs.
 */
@JsonClass(generateAdapter = true)
data class HabitLogDto(
    @Json(name = "id") val id: String,
    @Json(name = "habitId") val habitId: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "completedAt") val completedAt: Instant,
    @Json(name = "mood") val mood: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "createdAt") val createdAt: Instant
) {
    fun toEntity(): HabitLogEntity {
        return HabitLogEntity(
            id = id,
            habitId = habitId,
            userId = userId,
            completedAt = completedAt,
            mood = mood?.let { 
                try { HabitMood.valueOf(it) } catch (e: Exception) { null }
            },
            note = note,
            syncState = SyncState.SYNCED,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: HabitLogEntity): HabitLogDto {
            return HabitLogDto(
                id = entity.id,
                habitId = entity.habitId,
                userId = entity.userId,
                completedAt = entity.completedAt,
                mood = entity.mood?.name,
                note = entity.note,
                createdAt = entity.createdAt
            )
        }
    }
}
