package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.domain.model.Book
import com.ninety5.habitate.domain.model.Meal
import com.ninety5.habitate.domain.model.Quote
import com.ninety5.habitate.domain.model.Weather

interface PublicApiRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<Weather>
    suspend fun getRandomQuote(): Result<Quote>
    suspend fun searchBooks(query: String): Result<List<Book>>
    suspend fun getRandomMeal(): Result<Meal>
    suspend fun searchMeal(name: String): Result<List<Meal>>
}
