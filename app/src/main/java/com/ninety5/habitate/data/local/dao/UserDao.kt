package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninety5.habitate.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdOnce(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("UPDATE users SET isOnline = :isOnline, lastActive = :lastActive WHERE id = :userId")
    suspend fun updatePresence(userId: String, isOnline: Boolean, lastActive: Long)

    @Query("UPDATE users SET isStealthMode = :isStealthMode WHERE id = :userId")
    suspend fun updateStealthMode(userId: String, isStealthMode: Boolean)

    @Query("SELECT * FROM users WHERE username LIKE :query OR displayName LIKE :query ORDER BY displayName ASC LIMIT 50")
    suspend fun searchUsers(query: String): List<UserEntity>
}
