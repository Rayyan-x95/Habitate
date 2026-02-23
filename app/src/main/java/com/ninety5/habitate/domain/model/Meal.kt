package com.ninety5.habitate.domain.model

/**
 * Domain model for a meal.
 */
data class Meal(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String
)
