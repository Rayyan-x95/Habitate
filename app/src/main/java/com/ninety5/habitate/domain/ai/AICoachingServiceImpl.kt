package com.ninety5.habitate.domain.ai

import com.ninety5.habitate.BuildConfig
import com.ninety5.habitate.data.remote.OpenAiApi
import com.ninety5.habitate.data.remote.dto.ChatCompletionRequest
import com.ninety5.habitate.data.remote.dto.Message
import javax.inject.Inject

class AICoachingServiceImpl @Inject constructor(
    private val openAiApi: OpenAiApi
) : AICoachingService {
    override suspend fun getDailyAdvice(userId: String): String {
        return try {
            if (BuildConfig.OPENAI_API_KEY.isBlank()) {
                return "AI Coaching is currently unavailable. Please check back later."
            }

            val request = ChatCompletionRequest(
                messages = listOf(
                    Message(role = "system", content = "You are a helpful habit coaching assistant. Give a short, motivating tip for the day."),
                    Message(role = "user", content = "Give me advice for today.")
                )
            )
            val response = openAiApi.createChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )
            response.choices.firstOrNull()?.message?.content ?: "Stay consistent and keep moving forward!"
        } catch (e: Exception) {
            "Stay consistent and keep moving forward! (Offline Mode)"
        }
    }
}
