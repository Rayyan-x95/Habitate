package com.ninety5.habitate.core.di

import android.content.Context
import androidx.room.Room
import com.ninety5.habitate.data.local.HabitateDatabase
import com.ninety5.habitate.data.local.dao.HabitatDao
import com.ninety5.habitate.data.local.dao.JournalDao
import com.ninety5.habitate.data.local.dao.NotificationDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.dao.CommentDao
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.HabitLogDao
import com.ninety5.habitate.data.local.dao.HabitStreakDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HabitateDatabase {
        return Room.databaseBuilder(
            context,
            HabitateDatabase::class.java,
            HabitateDatabase.DATABASE_NAME
        )
            .addMigrations(HabitateDatabase.MIGRATION_1_2, HabitateDatabase.MIGRATION_2_3, HabitateDatabase.MIGRATION_3_4, HabitateDatabase.MIGRATION_4_5, HabitateDatabase.MIGRATION_5_6, HabitateDatabase.MIGRATION_6_7, HabitateDatabase.MIGRATION_7_8, HabitateDatabase.MIGRATION_8_9, HabitateDatabase.MIGRATION_9_10, HabitateDatabase.MIGRATION_10_11, HabitateDatabase.MIGRATION_11_12, HabitateDatabase.MIGRATION_12_13, HabitateDatabase.MIGRATION_13_14, HabitateDatabase.MIGRATION_14_15, HabitateDatabase.MIGRATION_15_16, HabitateDatabase.MIGRATION_16_17, HabitateDatabase.MIGRATION_17_18, HabitateDatabase.MIGRATION_18_19, HabitateDatabase.MIGRATION_19_20, HabitateDatabase.MIGRATION_20_21, HabitateDatabase.MIGRATION_21_22, HabitateDatabase.MIGRATION_22_23, HabitateDatabase.MIGRATION_23_24, HabitateDatabase.MIGRATION_24_25, HabitateDatabase.MIGRATION_25_26, HabitateDatabase.MIGRATION_26_27)
            .build()
    }

    @Provides
    fun provideTimelineDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.TimelineDao = database.timelineDao()

    @Provides
    fun provideChatDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.ChatDao = database.chatDao()

    @Provides
    fun provideDailySummaryDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.DailySummaryDao = database.dailySummaryDao()

    @Provides
    fun provideMessageReactionDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.MessageReactionDao = database.messageReactionDao()

    @Provides
    fun provideUserDao(database: HabitateDatabase): UserDao = database.userDao()

    @Provides
    fun provideFollowDao(database: HabitateDatabase): FollowDao = database.followDao()
    
    @Provides
    fun provideLikeDao(database: HabitateDatabase): LikeDao = database.likeDao()
    
    @Provides
    fun provideCommentDao(database: HabitateDatabase): CommentDao = database.commentDao()

    @Provides
    fun provideHabitDao(database: HabitateDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitLogDao(database: HabitateDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    fun provideHabitStreakDao(database: HabitateDatabase): HabitStreakDao = database.habitStreakDao()

    @Provides
    fun providePostDao(database: HabitateDatabase): PostDao = database.postDao()

    @Provides
    fun provideTaskDao(database: HabitateDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideWorkoutDao(database: HabitateDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideFocusDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.FocusDao = database.focusDao()

    @Provides
    fun provideHabitatDao(database: HabitateDatabase): HabitatDao = database.habitatDao()

    @Provides
    fun provideJournalDao(database: HabitateDatabase): JournalDao = database.journalDao()

    @Provides
    fun provideMessageDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.MessageDao = database.messageDao()

    @Provides
    fun provideStoryDao(database: HabitateDatabase): StoryDao = database.storyDao()

    @Provides
    fun provideStoryViewDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.StoryViewDao = database.storyViewDao()

    @Provides
    fun provideStoryMuteDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.StoryMuteDao = database.storyMuteDao()

    @Provides
    fun provideNotificationDao(database: HabitateDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideChallengeDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.ChallengeDao = database.challengeDao()

    @Provides
    fun provideSyncQueueDao(database: HabitateDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    fun provideInsightDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.InsightDao = database.insightDao()

    @Provides
    fun provideRemoteKeysDao(database: HabitateDatabase): com.ninety5.habitate.data.local.dao.RemoteKeysDao = database.remoteKeysDao()
}
