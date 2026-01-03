package com.ninety5.habitate.domain.ai

import javax.inject.Inject

class AICoachingServiceImpl @Inject constructor() : AICoachingService {
    override suspend fun getDailyAdvice(userId: String): String {
        return "Stay hydrated and keep moving!"
    }
}
