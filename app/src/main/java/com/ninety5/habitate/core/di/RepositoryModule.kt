package com.ninety5.habitate.core.di

import com.ninety5.habitate.data.repository.PublicApiRepositoryImpl
import com.ninety5.habitate.domain.repository.PublicApiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPublicApiRepository(
        publicApiRepositoryImpl: PublicApiRepositoryImpl
    ): PublicApiRepository
}
