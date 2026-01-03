package com.ninety5.habitate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.paging.PagingSource
import com.ninety5.habitate.data.local.entity.PostEntity
import com.ninety5.habitate.data.local.relation.PostWithDetails
import kotlinx.coroutines.flow.Flow

import com.ninety5.habitate.data.local.entity.SyncState

@Dao
interface PostDao {
    @Transaction
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostWithDetails>>

    @Transaction
    @Query("SELECT * FROM posts WHERE visibility = 'PUBLIC' ORDER BY createdAt DESC")
    fun getPublicPosts(): Flow<List<PostWithDetails>>

    @Transaction
    @Query("SELECT * FROM posts WHERE visibility = 'PUBLIC' ORDER BY createdAt DESC")
    fun getPublicPostsPaging(): PagingSource<Int, PostWithDetails>

    @Transaction
    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): Flow<List<PostWithDetails>>

    @Transaction
    @Query("SELECT * FROM posts WHERE habitatId = :habitatId ORDER BY createdAt DESC")
    fun getPostsByHabitat(habitatId: String): Flow<List<PostWithDetails>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostByIdOneShot(id: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("SELECT * FROM posts WHERE contentText LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM posts WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncPosts(): List<PostEntity>

    @Query("UPDATE posts SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)

    @Query("UPDATE posts SET isLiked = :isLiked, likesCount = :count, reactionType = :reactionType WHERE id = :id")
    suspend fun updateLikeStatus(id: String, isLiked: Boolean, count: Int, reactionType: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PostEntity)

    @Query("UPDATE posts SET isArchived = 1, syncState = 'PENDING', updatedAt = :now WHERE createdAt < :cutoff")
    suspend fun archiveOldPosts(cutoff: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE posts SET isArchived = 0, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun restorePost(id: String, now: Long = System.currentTimeMillis())
}
