package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.data.remote.publicapis.BookDto
import com.ninety5.habitate.data.remote.publicapis.MealDto
import com.ninety5.habitate.data.remote.publicapis.QuoteDto
import com.ninety5.habitate.data.remote.publicapis.WeatherResponse

interface PublicApiRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherResponse>
    suspend fun getRandomQuote(): Result<QuoteDto>
    suspend fun searchBooks(query: String): Result<List<BookDto>>
    suspend fun getRandomMeal(): Result<MealDto>
    suspend fun searchMeal(name: String): Result<List<MealDto>>
}
