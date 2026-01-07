package com.ninety5.habitate.data.remote.publicapis

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApi {
    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): MealResponse

    @GET("api/json/v1/1/search.php")
    suspend fun searchMeal(@Query("s") name: String): MealResponse
}

data class MealResponse(
    @Json(name = "meals") val meals: List<MealDto>?
)

data class MealDto(
    @Json(name = "idMeal") val id: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strCategory") val category: String,
    @Json(name = "strArea") val area: String,
    @Json(name = "strInstructions") val instructions: String,
    @Json(name = "strMealThumb") val thumbUrl: String
)
