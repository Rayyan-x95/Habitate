package com.ninety5.habitate.data.remote.publicapis

import com.squareup.moshi.Json
import retrofit2.http.GET

interface QuotesApi {
    @GET("api/random")
    suspend fun getRandomQuote(): List<QuoteDto>
}

data class QuoteDto(
    @Json(name = "q") val quote: String,
    @Json(name = "a") val author: String,
    @Json(name = "h") val html: String? = null
)
