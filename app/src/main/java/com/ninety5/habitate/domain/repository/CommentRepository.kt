package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for comment operations.
 */
interface CommentRepository {
    fun getCommentsForPost(postId: String): Flow<List<Comment>>
    fun getCommentsCount(postId: String): Flow<Int>
    suspend fun createComment(postId: String, text: String): Result<String>
    suspend fun deleteComment(commentId: String): Result<Unit>
}
