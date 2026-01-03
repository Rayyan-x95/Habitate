package com.ninety5.habitate.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninety5.habitate.data.local.HabitateDatabase
import com.ninety5.habitate.data.local.entity.RemoteKeysEntity
import com.ninety5.habitate.data.local.relation.PostWithDetails
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.domain.mapper.toPostEntity
import com.ninety5.habitate.domain.mapper.toUserEntity
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class FeedRemoteMediator @Inject constructor(
    private val apiService: ApiService,
    private val database: HabitateDatabase
) : RemoteMediator<Int, PostWithDetails>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithDetails>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val apiResponse = apiService.getFeed(page = page, limit = state.config.pageSize)

            val endOfPaginationReached = apiResponse.isEmpty()
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                }
                
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = apiResponse.map {
                    RemoteKeysEntity(repoId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                
                database.remoteKeysDao().insertAll(keys)
                
                val posts = apiResponse.map { it.toPostEntity() }
                val authors = apiResponse.map { it.author.toUserEntity() }
                
                database.userDao().upsertAll(authors)
                database.postDao().upsertAll(posts)
            }
            
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostWithDetails>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { post ->
                database.remoteKeysDao().remoteKeysPostId(post.post.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, PostWithDetails>): RemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { post ->
                database.remoteKeysDao().remoteKeysPostId(post.post.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, PostWithDetails>): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.post?.id?.let { repoId ->
                database.remoteKeysDao().remoteKeysPostId(repoId)
            }
        }
    }
}
