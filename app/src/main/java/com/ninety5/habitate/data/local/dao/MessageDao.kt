package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.MessageStatus
import com.ninety5.habitate.data.local.relation.MessageWithReactions
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getMessages(chatId: String): Flow<List<MessageWithReactions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query("UPDATE messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: MessageStatus)

    @Query("SELECT * FROM messages WHERE status = 'SENDING'")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Query("UPDATE messages SET status = 'READ' WHERE chatId = :chatId AND status != 'READ'")
    suspend fun markAllAsRead(chatId: String)
}
