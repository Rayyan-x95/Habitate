package com.ninety5.habitate.domain.model

import java.time.LocalDate

/**
 * Domain model for a daily summary / check-in entry.
 */
data class DailySummary(
    val date: LocalDate,
    val steps: Int,
    val caloriesBurned: Double,
    val distanceMeters: Double,
    val activeMinutes: Int,
    val mood: String?,
    val notes: String?
)
