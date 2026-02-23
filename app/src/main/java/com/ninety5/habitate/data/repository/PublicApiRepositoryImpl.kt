package com.ninety5.habitate.data.repository

import com.ninety5.habitate.data.remote.publicapis.BooksApi
import com.ninety5.habitate.data.remote.publicapis.FoodApi
import com.ninety5.habitate.data.remote.publicapis.QuotesApi
import com.ninety5.habitate.data.remote.publicapis.WeatherApi
import com.ninety5.habitate.domain.mapper.toDomain
import com.ninety5.habitate.domain.model.Book
import com.ninety5.habitate.domain.model.Meal
import com.ninety5.habitate.domain.model.Quote
import com.ninety5.habitate.domain.model.Weather
import com.ninety5.habitate.domain.repository.PublicApiRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class PublicApiRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val quotesApi: QuotesApi,
    private val booksApi: BooksApi,
    private val foodApi: FoodApi
) : PublicApiRepository {

    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<Weather> {
        return try {
            val response = weatherApi.getCurrentWeather(latitude, longitude)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getRandomQuote(): Result<Quote> {
        return try {
            val response = quotesApi.getRandomQuote()
            if (response.isNotEmpty()) {
                Result.success(response.first().toDomain())
            } else {
                Result.failure(Exception("No quotes found"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val response = booksApi.searchBooks(query)
            Result.success(response.docs.map { it.toDomain() })
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getRandomMeal(): Result<Meal> {
        return try {
            val response = foodApi.getRandomMeal()
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                Result.success(meal.toDomain())
            } else {
                Result.failure(Exception("No meal found"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun searchMeal(name: String): Result<List<Meal>> {
        return try {
            val response = foodApi.searchMeal(name)
            Result.success((response.meals ?: emptyList()).map { it.toDomain() })
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
