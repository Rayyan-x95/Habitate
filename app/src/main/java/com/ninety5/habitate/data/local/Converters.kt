package com.ninety5.habitate.data.local

import androidx.room.TypeConverter
import com.ninety5.habitate.data.local.entity.HabitatPrivacy
import com.ninety5.habitate.data.local.entity.HabitatRole
import com.ninety5.habitate.data.local.entity.SyncState
import com.ninety5.habitate.data.local.entity.SyncStatus
import com.ninety5.habitate.data.local.entity.TaskStatus
import com.ninety5.habitate.data.local.entity.Visibility
import com.ninety5.habitate.data.local.entity.WorkoutSource
import com.ninety5.habitate.data.local.entity.HabitCategory
import com.ninety5.habitate.data.local.entity.HabitFrequency
import com.ninety5.habitate.data.local.entity.HabitMood
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.local.entity.ChatType
import com.ninety5.habitate.data.local.entity.InsightPriority
import com.ninety5.habitate.data.local.entity.InsightType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString("||")

    @TypeConverter
    fun toStringList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split("||")

    @TypeConverter
    fun fromSyncState(state: SyncState): String = state.name

    @TypeConverter
    fun toSyncState(value: String): SyncState = try {
        SyncState.valueOf(value)
    } catch (e: IllegalArgumentException) {
        SyncState.SYNCED // Safe default
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (e: IllegalArgumentException) {
        TaskStatus.OPEN // Safe default
    }

    @TypeConverter
    fun fromWorkoutSource(source: WorkoutSource): String = source.name

    @TypeConverter
    fun toWorkoutSource(value: String): WorkoutSource = try {
        WorkoutSource.valueOf(value)
    } catch (e: IllegalArgumentException) {
        WorkoutSource.MANUAL // Safe default
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = try {
        SyncStatus.valueOf(value)
    } catch (e: IllegalArgumentException) {
        SyncStatus.PENDING // Safe default
    }

    @TypeConverter
    fun fromVisibility(visibility: Visibility): String = visibility.name

    @TypeConverter
    fun toVisibility(value: String): Visibility = try {
        Visibility.valueOf(value)
    } catch (e: IllegalArgumentException) {
        Visibility.PUBLIC // Safe default
    }

    @TypeConverter
    fun fromHabitatPrivacy(privacy: HabitatPrivacy): String = privacy.name

    @TypeConverter
    fun toHabitatPrivacy(value: String): HabitatPrivacy = try {
        HabitatPrivacy.valueOf(value)
    } catch (e: IllegalArgumentException) {
        HabitatPrivacy.PUBLIC // Safe default
    }

    @TypeConverter
    fun fromHabitatRole(role: HabitatRole): String = role.name

    @TypeConverter
    fun toHabitatRole(value: String): HabitatRole = try {
        HabitatRole.valueOf(value)
    } catch (e: IllegalArgumentException) {
        HabitatRole.MEMBER // Safe default
    }

    // Habit-related converters
    @TypeConverter
    fun fromHabitCategory(category: HabitCategory): String = category.name

    @TypeConverter
    fun toHabitCategory(value: String): HabitCategory = try {
        HabitCategory.valueOf(value)
    } catch (e: IllegalArgumentException) {
        HabitCategory.OTHER // Safe default
    }

    @TypeConverter
    fun fromHabitFrequency(frequency: HabitFrequency): String = frequency.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = try {
        HabitFrequency.valueOf(value)
    } catch (e: IllegalArgumentException) {
        HabitFrequency.DAILY // Safe default
    }

    @TypeConverter
    fun fromHabitMood(mood: HabitMood?): String? = mood?.name

    @TypeConverter
    fun toHabitMood(value: String?): HabitMood? = value?.let { HabitMood.valueOf(it) }

    // Insight converters
    @TypeConverter
    fun fromInsightType(type: InsightType): String = type.name

    @TypeConverter
    fun toInsightType(value: String): InsightType = InsightType.valueOf(value)

    @TypeConverter
    fun fromInsightPriority(priority: InsightPriority): String = priority.name

    @TypeConverter
    fun toInsightPriority(value: String): InsightPriority = InsightPriority.valueOf(value)


    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? = days?.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek>? = 
        value?.split(",")?.map { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)

    @TypeConverter
    fun fromChatType(type: ChatType): String = type.name

    @TypeConverter
    fun toChatType(value: String): ChatType = ChatType.valueOf(value)
}
