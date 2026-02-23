package com.ninety5.habitate.domain.model

/**
 * Domain model for current weather data.
 */
data class Weather(
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val weatherCode: Int,
    val time: String
)
