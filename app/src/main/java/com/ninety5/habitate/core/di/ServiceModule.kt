package com.ninety5.habitate.core.di

import com.ninety5.habitate.domain.ai.AICoachingService
import com.ninety5.habitate.domain.ai.AICoachingServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindAICoachingService(
        impl: AICoachingServiceImpl
    ): AICoachingService
}
