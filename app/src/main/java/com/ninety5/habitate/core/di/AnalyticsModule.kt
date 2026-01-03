package com.ninety5.habitate.core.di

import com.ninety5.habitate.core.analytics.AnalyticsManager
import com.ninety5.habitate.core.analytics.FirebaseAnalyticsManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsManager(
        analyticsManager: FirebaseAnalyticsManager
    ): AnalyticsManager
}
