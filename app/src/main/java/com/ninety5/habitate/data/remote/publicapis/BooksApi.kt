package com.ninety5.habitate.data.remote.publicapis

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): BookSearchResponse
}

data class BookSearchResponse(
    @Json(name = "numFound") val numFound: Int,
    @Json(name = "docs") val docs: List<BookDto>
)

data class BookDto(
    @Json(name = "key") val key: String,
    @Json(name = "title") val title: String,
    @Json(name = "author_name") val authorName: List<String>? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "cover_i") val coverId: Int? = null
)
