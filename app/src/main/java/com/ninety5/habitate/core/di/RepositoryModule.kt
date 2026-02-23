package com.ninety5.habitate.core.di

import com.ninety5.habitate.data.repository.AuthRepositoryImpl
import com.ninety5.habitate.data.repository.ChallengeRepositoryImpl
import com.ninety5.habitate.data.repository.ChatRepositoryImpl
import com.ninety5.habitate.data.repository.CommentRepositoryImpl
import com.ninety5.habitate.data.repository.DailyCheckInRepositoryImpl
import com.ninety5.habitate.data.repository.FeedRepositoryImpl
import com.ninety5.habitate.data.repository.FocusRepositoryImpl
import com.ninety5.habitate.data.repository.HabitRepositoryImpl
import com.ninety5.habitate.data.repository.HabitatRepositoryImpl
import com.ninety5.habitate.data.repository.InsightRepositoryImpl
import com.ninety5.habitate.data.repository.JournalRepositoryImpl
import com.ninety5.habitate.data.repository.MediaRepositoryImpl
import com.ninety5.habitate.data.repository.NotificationRepositoryImpl
import com.ninety5.habitate.data.repository.PublicApiRepositoryImpl
import com.ninety5.habitate.data.repository.StoryRepositoryImpl
import com.ninety5.habitate.data.repository.TaskRepositoryImpl
import com.ninety5.habitate.data.repository.TimelineRepositoryImpl
import com.ninety5.habitate.data.repository.UserPreferencesRepositoryImpl
import com.ninety5.habitate.data.repository.UserRepositoryImpl
import com.ninety5.habitate.data.repository.WorkoutRepositoryImpl
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.domain.repository.ChallengeRepository
import com.ninety5.habitate.domain.repository.ChatRepository
import com.ninety5.habitate.domain.repository.CommentRepository
import com.ninety5.habitate.domain.repository.DailyCheckInRepository
import com.ninety5.habitate.domain.repository.FeedRepository
import com.ninety5.habitate.domain.repository.FocusRepository
import com.ninety5.habitate.domain.repository.HabitRepository
import com.ninety5.habitate.domain.repository.HabitatRepository
import com.ninety5.habitate.domain.repository.InsightRepository
import com.ninety5.habitate.domain.repository.JournalRepository
import com.ninety5.habitate.domain.repository.MediaRepository
import com.ninety5.habitate.domain.repository.NotificationRepository
import com.ninety5.habitate.domain.repository.PublicApiRepository
import com.ninety5.habitate.domain.repository.StoryRepository
import com.ninety5.habitate.domain.repository.TaskRepository
import com.ninety5.habitate.domain.repository.TimelineRepository
import com.ninety5.habitate.domain.repository.UserPreferencesRepository
import com.ninety5.habitate.domain.repository.UserRepository
import com.ninety5.habitate.domain.repository.WorkoutRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        feedRepositoryImpl: FeedRepositoryImpl
    ): FeedRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindInsightRepository(
        insightRepositoryImpl: InsightRepositoryImpl
    ): InsightRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindFocusRepository(
        focusRepositoryImpl: FocusRepositoryImpl
    ): FocusRepository

    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        journalRepositoryImpl: JournalRepositoryImpl
    ): JournalRepository

    @Binds
    @Singleton
    abstract fun bindHabitatRepository(
        habitatRepositoryImpl: HabitatRepositoryImpl
    ): HabitatRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindDailyCheckInRepository(
        dailyCheckInRepositoryImpl: DailyCheckInRepositoryImpl
    ): DailyCheckInRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(
        timelineRepositoryImpl: TimelineRepositoryImpl
    ): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
