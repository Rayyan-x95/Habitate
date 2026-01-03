package com.ninety5.habitate.core.di

import android.content.Context
import com.ninety5.habitate.core.glyph.HabitateGlyphManager
import com.ninety5.habitate.core.utils.GlyphInterfaceManager
import com.ninety5.habitate.core.utils.GlyphMatrixController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlyphModule {

    @Provides
    @Singleton
    fun provideGlyphInterfaceManager(@ApplicationContext context: Context): GlyphInterfaceManager {
        return GlyphInterfaceManager(context)
    }

    @Provides
    @Singleton
    fun provideGlyphMatrixController(@ApplicationContext context: Context): GlyphMatrixController {
        return GlyphMatrixController(context)
    }
    
    @Provides
    @Singleton
    fun provideHabitateGlyphManager(
        @ApplicationContext context: Context
    ): HabitateGlyphManager {
        return HabitateGlyphManager(context).apply {
            init()
        }
    }
}
