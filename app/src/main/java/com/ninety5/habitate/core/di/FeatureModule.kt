package com.ninety5.habitate.core.di

import com.ninety5.habitate.util.FeatureFlags
import com.ninety5.habitate.util.FeatureFlagsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureModule {

    @Binds
    @Singleton
    abstract fun bindFeatureFlags(
        featureFlagsImpl: FeatureFlagsImpl
    ): FeatureFlags
}
