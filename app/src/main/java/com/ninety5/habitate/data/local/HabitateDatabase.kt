package com.ninety5.habitate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ninety5.habitate.data.local.dao.HabitatDao
import com.ninety5.habitate.data.local.dao.JournalDao
import com.ninety5.habitate.data.local.dao.MessageDao
import com.ninety5.habitate.data.local.dao.NotificationDao
import com.ninety5.habitate.data.local.dao.PostDao
import com.ninety5.habitate.data.local.dao.StoryDao
import com.ninety5.habitate.data.local.dao.SyncQueueDao
import com.ninety5.habitate.data.local.dao.TaskDao
import com.ninety5.habitate.data.local.dao.UserDao
import com.ninety5.habitate.data.local.dao.WorkoutDao
import com.ninety5.habitate.data.local.dao.HabitDao
import com.ninety5.habitate.data.local.dao.HabitLogDao
import com.ninety5.habitate.data.local.dao.HabitStreakDao
import com.ninety5.habitate.data.local.entity.DailySummaryEntity
import com.ninety5.habitate.data.local.entity.HabitatEntity
import com.ninety5.habitate.data.local.entity.ChatEntity
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.NotificationEntity
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.entity.StoryEntity
import com.ninety5.habitate.data.local.entity.SyncOperationEntity
import com.ninety5.habitate.data.local.entity.TaskCategoryEntity
import com.ninety5.habitate.data.local.entity.TaskCompletionEntity
import com.ninety5.habitate.data.local.entity.TaskEntity
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.local.entity.WorkoutEntity
import com.ninety5.habitate.data.local.entity.HabitatMembershipEntity
import com.ninety5.habitate.data.local.entity.JournalEntryEntity

import com.ninety5.habitate.data.local.dao.FollowDao
import com.ninety5.habitate.data.local.dao.LikeDao
import com.ninety5.habitate.data.local.dao.CommentDao
import com.ninety5.habitate.data.local.entity.FollowEntity
import com.ninety5.habitate.data.local.entity.LikeEntity
import com.ninety5.habitate.data.local.entity.CommentEntity
import com.ninety5.habitate.data.local.entity.HabitEntity
import com.ninety5.habitate.data.local.entity.HabitLogEntity
import com.ninety5.habitate.data.local.entity.HabitStreakEntity

/**
 * Main Room database for the Habitate app.
 * Contains all entities and provides access to DAOs.
 */
import com.ninety5.habitate.data.local.dao.RemoteKeysDao
import com.ninety5.habitate.data.local.entity.RemoteKeysEntity

import com.ninety5.habitate.data.local.entity.MessageReactionEntity

import com.ninety5.habitate.data.local.entity.StoryViewEntity
import com.ninety5.habitate.data.local.entity.StoryMuteEntity

@Database(
    entities = [
        // User
        UserEntity::class,
        FollowEntity::class,  // Updated from FollowingEntity with composite key
        // Social Interactions
        LikeEntity::class,     // NEW: Like relationships
        CommentEntity::class,  // NEW: Comment system
        // Habits (CORE FEATURE)
        HabitEntity::class,
        HabitLogEntity::class,
        HabitStreakEntity::class,
        // Social
        PostEntity::class,
        StoryEntity::class,
        StoryViewEntity::class,
        StoryMuteEntity::class,
        MessageEntity::class,
        MessageReactionEntity::class,
        ChatEntity::class,
        // Tasks & Habits
        TaskEntity::class,
        TaskCategoryEntity::class,
        TaskCompletionEntity::class,
        // Fitness
        WorkoutEntity::class,
        // Focus
        com.ninety5.habitate.data.local.entity.FocusSessionEntity::class,
        DailySummaryEntity::class,
        // Habitats (Groups)
        HabitatEntity::class,
        HabitatMembershipEntity::class,
        // Journal
        JournalEntryEntity::class,
        // Notifications
        NotificationEntity::class,
        // Sync
        SyncOperationEntity::class,
        // Paging
        RemoteKeysEntity::class,
        // Chat
        com.ninety5.habitate.data.local.entity.ChallengeEntity::class,
        com.ninety5.habitate.data.local.entity.ChallengeProgressEntity::class,
        com.ninety5.habitate.data.local.entity.InsightEntity::class
    ],
    views = [com.ninety5.habitate.data.local.view.TimelineItem::class],
    version = 26, // Updated to include index optimization migration (25->26)
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitateDatabase : RoomDatabase() {
    abstract fun timelineDao(): com.ninety5.habitate.data.local.dao.TimelineDao

    abstract fun chatDao(): com.ninety5.habitate.data.local.dao.ChatDao
    abstract fun messageReactionDao(): com.ninety5.habitate.data.local.dao.MessageReactionDao
    abstract fun challengeDao(): com.ninety5.habitate.data.local.dao.ChallengeDao
    abstract fun userDao(): UserDao
    abstract fun followDao(): FollowDao  // Updated from followingDao for composite key support
    abstract fun likeDao(): LikeDao      // NEW: Like operations
    abstract fun commentDao(): CommentDao // NEW: Comment operations
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitStreakDao(): HabitStreakDao
    abstract fun focusDao(): com.ninety5.habitate.data.local.dao.FocusDao
    abstract fun dailySummaryDao(): com.ninety5.habitate.data.local.dao.DailySummaryDao
    abstract fun postDao(): PostDao
    abstract fun taskDao(): TaskDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun habitatDao(): HabitatDao
    abstract fun journalDao(): JournalDao
    abstract fun messageDao(): MessageDao
    abstract fun storyDao(): StoryDao
    abstract fun storyViewDao(): com.ninety5.habitate.data.local.dao.StoryViewDao
    abstract fun storyMuteDao(): com.ninety5.habitate.data.local.dao.StoryMuteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun insightDao(): com.ninety5.habitate.data.local.dao.InsightDao

    companion object {
        const val DATABASE_NAME = "habitate.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceRule TEXT")
                db.execSQL("ALTER TABLE workouts ADD COLUMN externalId TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_externalId ON workouts(externalId)")
            }
        }
        
        /**
         * Migration from version 2 to 3:
         * - Creates 'likes' table with composite primary key (userId, postId)
         * - Creates 'comments' table with proper foreign keys
         * - Renames 'following' table to 'follows' and adds followerId column for composite key
         * - Adds new columns to 'users' table (email, createdAt, followerCount, followingCount, postCount)
         * 
         * BREAKING CHANGES:
         * - FollowingEntity → FollowEntity with composite key (followerId, followingId)
         * - UserEntity now requires email (unique) and timestamps
         * 
         * This migration enables full social features:
         * - Like tracking per user per post
         * - Comment system with user attribution
         * - Bidirectional follow relationships
         * - User stats (followers, following, posts count)
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. CREATE LIKES TABLE
                // Composite primary key (userId, postId) ensures one like per user per post
                // Foreign keys with CASCADE delete to maintain referential integrity
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `likes` (
                        `userId` TEXT NOT NULL,
                        `postId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`userId`, `postId`),
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
                        FOREIGN KEY(`postId`) REFERENCES `posts`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION
                    )
                """.trimIndent())
                
                // Indexes for efficient queries
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_userId` ON `likes` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_postId` ON `likes` (`postId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_syncState` ON `likes` (`syncState`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_likes_createdAt` ON `likes` (`createdAt`)")
                
                // 2. CREATE COMMENTS TABLE
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `comments` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `postId` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `syncState` TEXT NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
                        FOREIGN KEY(`postId`) REFERENCES `posts`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION
                    )
                """.trimIndent())
                
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_comments_userId` ON `comments` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_comments_postId` ON `comments` (`postId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_comments_createdAt` ON `comments` (`createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_comments_syncState` ON `comments` (`syncState`)")
                
                // 3. MIGRATE FOLLOWS (from 'following' table to 'follows' with composite key)
                // Create new table with composite primary key (followerId, followingId)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `follows` (
                        `followerId` TEXT NOT NULL,
                        `followingId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`followerId`, `followingId`),
                        FOREIGN KEY(`followerId`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
                        FOREIGN KEY(`followingId`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION
                    )
                """.trimIndent())
                
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_follows_followerId` ON `follows` (`followerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_follows_followingId` ON `follows` (`followingId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_follows_syncState` ON `follows` (`syncState`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_follows_createdAt` ON `follows` (`createdAt`)")
                
                // Migrate data from old 'following' table if it exists
                // Old schema had single key 'userId', new schema needs 'followerId' and 'followingId'
                // Since old table didn't track bidirectional relationships properly, we'll start fresh
                db.execSQL("DROP TABLE IF EXISTS `following`")
                
                // 4. ALTER USERS TABLE (add new columns)
                // Add email with unique constraint for authentication
                db.execSQL("ALTER TABLE `users` ADD COLUMN `email` TEXT DEFAULT NULL")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
                
                // Add timestamp for user creation tracking
                db.execSQL("ALTER TABLE `users` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                
                // Add denormalized counts for performance (updated by triggers or app logic)
                db.execSQL("ALTER TABLE `users` ADD COLUMN `followerCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `followingCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `postCount` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `remote_keys` (
                        `repoId` TEXT NOT NULL PRIMARY KEY,
                        `prevKey` INTEGER,
                        `nextKey` INTEGER
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create focus_sessions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `focus_sessions` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER,
                        `durationSeconds` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `syncState` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Drop old study_sessions table
                db.execSQL("DROP TABLE IF EXISTS `study_sessions`")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_messages` (
                        `id` TEXT NOT NULL,
                        `roomId` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_rooms` (
                        `id` TEXT NOT NULL,
                        `name` TEXT,
                        `type` TEXT NOT NULL,
                        `lastMessage` TEXT,
                        `lastMessageTimestamp` INTEGER,
                        `unreadCount` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `stories` ADD COLUMN `syncState` TEXT NOT NULL DEFAULT 'SYNCED'")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `challenges` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `metricType` TEXT NOT NULL,
                        `targetValue` REAL NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `creatorId` TEXT,
                        `habitatId` TEXT,
                        `syncState` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `chat_rooms`")
                db.execSQL("DROP TABLE IF EXISTS `chat_messages`")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chats` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT,
                        `lastMessage` TEXT,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `id` TEXT NOT NULL,
                        `chatId` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `content` TEXT,
                        `mediaUrl` TEXT,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)

                // Add syncState to stories
                db.execSQL("ALTER TABLE `stories` ADD COLUMN `syncState` TEXT NOT NULL DEFAULT 'SYNCED'")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN isOnline INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN lastActive INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chats ADD COLUMN isMuted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `message_reactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `emoji` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_reactions_messageId` ON `message_reactions` (`messageId`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN caption TEXT")
                db.execSQL("ALTER TABLE stories ADD COLUMN visibility TEXT NOT NULL DEFAULT 'PUBLIC'")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `story_views` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `storyId` TEXT NOT NULL,
                        `viewerId` TEXT NOT NULL,
                        `viewedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`storyId`) REFERENCES `stories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_story_views_storyId` ON `story_views` (`storyId`)")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `story_mutes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `mutedUserId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE VIEW `timeline_view` AS 
                    SELECT id, 'post' as type, createdAt as timestamp, contentText as title, NULL as subtitle FROM posts
                    UNION ALL
                    SELECT id, 'workout' as type, startTs as timestamp, type as title, NULL as subtitle FROM workouts
                    UNION ALL
                    SELECT id, 'story' as type, createdAt as timestamp, caption as title, NULL as subtitle FROM stories
                    UNION ALL
                    SELECT id, 'task' as type, updatedAt as timestamp, title as title, status as subtitle FROM tasks WHERE status = 'DONE'
                """)
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isArchived to posts
                db.execSQL("ALTER TABLE posts ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                
                // Add isArchived to workouts
                db.execSQL("ALTER TABLE workouts ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                
                // Recreate timeline_view with isArchived
                db.execSQL("DROP VIEW IF EXISTS timeline_view")
                db.execSQL("""
                    CREATE VIEW `timeline_view` AS 
                    SELECT id, 'post' as type, createdAt as timestamp, contentText as title, NULL as subtitle, isArchived FROM posts
                    UNION ALL
                    SELECT id, 'workout' as type, startTs as timestamp, type as title, NULL as subtitle, isArchived FROM workouts
                    UNION ALL
                    SELECT id, 'story' as type, createdAt as timestamp, caption as title, NULL as subtitle, 0 as isArchived FROM stories
                    UNION ALL
                    SELECT id, 'task' as type, updatedAt as timestamp, title as title, status as subtitle, CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END as isArchived FROM tasks WHERE status = 'DONE' OR status = 'ARCHIVED'
                """)
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add soundTrack and rating to focus_sessions
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN soundTrack TEXT")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN rating INTEGER")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_summaries ADD COLUMN mood TEXT")
                db.execSQL("ALTER TABLE daily_summaries ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE likes ADD COLUMN reactionType TEXT NOT NULL DEFAULT 'HEART'")
                db.execSQL("ALTER TABLE posts ADD COLUMN reactionType TEXT")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN isStealthMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `challenge_progress` (
                        `id` TEXT NOT NULL,
                        `challengeId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `progress` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `joinedAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncState` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `insights` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `relatedEntityId` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `isDismissed` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'MEDIUM'")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP VIEW IF EXISTS timeline_view")
                db.execSQL("""
                    CREATE VIEW `timeline_view` AS 
                    SELECT id, 'post' as type, createdAt as timestamp, contentText as title, NULL as subtitle, isArchived FROM posts
                    UNION ALL
                    SELECT id, 'workout' as type, startTs as timestamp, type as title, NULL as subtitle, isArchived FROM workouts
                    UNION ALL
                    SELECT id, 'story' as type, createdAt as timestamp, caption as title, NULL as subtitle, 0 as isArchived FROM stories
                    UNION ALL
                    SELECT id, 'task' as type, updatedAt as timestamp, title as title, status as subtitle, CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END as isArchived FROM tasks WHERE status = 'DONE' OR status = 'ARCHIVED'
                    UNION ALL
                    SELECT id, 'insight' as type, createdAt as timestamp, title as title, description as subtitle, isDismissed as isArchived FROM insights
                    UNION ALL
                    SELECT id, 'journal' as type, date as timestamp, COALESCE(title, 'Journal Entry') as title, content as subtitle, 0 as isArchived FROM journal_entries
                """)
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN isSaved INTEGER NOT NULL DEFAULT 0")
                db.execSQL("DROP VIEW IF EXISTS timeline_view")
                db.execSQL("""
                    CREATE VIEW `timeline_view` AS 
                    SELECT id, 'post' as type, createdAt as timestamp, contentText as title, NULL as subtitle, isArchived FROM posts
                    UNION ALL
                    SELECT id, 'workout' as type, startTs as timestamp, type as title, NULL as subtitle, isArchived FROM workouts
                    UNION ALL
                    SELECT id, 'story' as type, createdAt as timestamp, caption as title, NULL as subtitle, 0 as isArchived FROM stories WHERE isSaved = 1
                    UNION ALL
                    SELECT id, 'task' as type, updatedAt as timestamp, title as title, status as subtitle, CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END as isArchived FROM tasks WHERE status = 'DONE' OR status = 'ARCHIVED'
                    UNION ALL
                    SELECT id, 'insight' as type, createdAt as timestamp, title as title, description as subtitle, isDismissed as isArchived FROM insights
                    UNION ALL
                    SELECT id, 'journal' as type, date as timestamp, COALESCE(title, 'Journal Entry') as title, content as subtitle, 0 as isArchived FROM journal_entries
                """)
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN linkedEntityId TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN linkedEntityType TEXT")
                db.execSQL("ALTER TABLE posts ADD COLUMN linkedEntityId TEXT")
                db.execSQL("ALTER TABLE posts ADD COLUMN linkedEntityType TEXT")
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notifications ADD COLUMN isDigest INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add indices to journal_entries for query optimization
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_userId ON journal_entries(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_date ON journal_entries(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_mood ON journal_entries(mood)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_syncState ON journal_entries(syncState)")
                
                // Add indices to notifications for query optimization
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userId ON notifications(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isRead ON notifications(isRead)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_createdAt ON notifications(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_type ON notifications(type)")
            }
        }
    }
}
