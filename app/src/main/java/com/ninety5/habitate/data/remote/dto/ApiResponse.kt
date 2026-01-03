package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val data: T?,
    val error: ApiError?
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String>?
)
