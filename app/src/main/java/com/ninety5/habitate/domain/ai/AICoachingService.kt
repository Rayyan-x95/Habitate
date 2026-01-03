package com.ninety5.habitate.domain.ai

interface AICoachingService {
    suspend fun getDailyAdvice(userId: String): String
}
