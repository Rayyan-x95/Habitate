package com.ninety5.habitate.data.remote

import com.ninety5.habitate.data.remote.dto.ChatCompletionRequest
import com.ninety5.habitate.data.remote.dto.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
