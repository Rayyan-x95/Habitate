package com.ninety5.habitate.data.remote.publicapis

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse
}

data class WeatherResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeather
)

data class CurrentWeather(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "windspeed") val windSpeed: Double,
    @Json(name = "winddirection") val windDirection: Double,
    @Json(name = "weathercode") val weatherCode: Int,
    @Json(name = "time") val time: String
)
