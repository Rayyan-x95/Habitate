package com.ninety5.habitate.data.remote

import retrofit2.http.*

import com.ninety5.habitate.data.remote.dto.PostDto
import com.ninety5.habitate.data.remote.dto.NotificationDto
import com.ninety5.habitate.data.remote.dto.UserDto
import com.ninety5.habitate.data.remote.dto.TaskDto
import com.ninety5.habitate.data.remote.dto.WorkoutDto
import com.ninety5.habitate.data.remote.dto.HabitatDto
import com.ninety5.habitate.data.remote.dto.StoryDto
import com.ninety5.habitate.data.remote.dto.ChallengeDto
import com.ninety5.habitate.data.remote.dto.ChallengeCreateRequest
import com.ninety5.habitate.data.remote.dto.ChallengeParticipantDto
import com.ninety5.habitate.data.remote.dto.LeaderboardEntryDto
import com.ninety5.habitate.data.remote.dto.JournalEntryDto
import com.ninety5.habitate.data.remote.dto.JournalEntryCreateRequest
import com.ninety5.habitate.data.remote.dto.ChatDto
import com.ninety5.habitate.data.remote.dto.MessageDto
import com.ninety5.habitate.data.remote.dto.HabitDto
import com.ninety5.habitate.data.remote.dto.HabitLogDto

import okhttp3.MultipartBody

import com.ninety5.habitate.data.remote.dto.AuthResponse
import com.ninety5.habitate.data.remote.dto.RefreshTokenRequest
import com.ninety5.habitate.data.remote.dto.RegisterRequest
import retrofit2.Response

interface ApiService {
    // Auth
    @POST("auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // Chat
    @GET("chats")
    suspend fun getChats(): List<ChatDto>

    @GET("chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String, @Query("since") since: Long?): List<MessageDto>

    @POST("chats/{chatId}/read")
    suspend fun markChatRead(@Path("chatId") chatId: String)

    // Feed & Posts
    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<PostDto>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead()

    // Habitats
    @GET("habitats")
    suspend fun getHabitats(): List<HabitatDto>

    // Users
    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") id: String)

    @POST("users/{id}/unfollow")
    suspend fun unfollowUser(@Path("id") id: String)

    @GET("users/{id}/followers")
    suspend fun getUserFollowers(
        @Path("id") id: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<UserDto>

    @GET("users/{id}/following")
    suspend fun getUserFollowing(
        @Path("id") id: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<UserDto>

    // Uploads
    @Multipart
    @POST("uploads")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): String

    // Tasks
    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): TaskDto

    // Workouts
    @GET("workouts")
    suspend fun getWorkouts(): List<WorkoutDto>

    // Habits
    @GET("habits")
    suspend fun getHabits(): List<HabitDto>

    @GET("habits/{id}/logs")
    suspend fun getHabitLogs(
        @Path("id") habitId: String,
        @Query("since") since: Long? = null
    ): List<HabitLogDto>

    // Stories
    @GET("stories")
    suspend fun getStories(): List<StoryDto>

    // Challenges
    @GET("challenges")
    suspend fun getChallenges(
        @Query("habitat_id") habitatId: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<ChallengeDto>

    @POST("challenges/{id}/join")
    suspend fun joinChallenge(@Path("id") id: String)

    @POST("challenges/{id}/progress")
    suspend fun updateChallengeProgress(
        @Path("id") id: String,
        @Query("value") value: Double
    ): ChallengeParticipantDto

    @GET("challenges/{id}/leaderboard")
    suspend fun getChallengeLeaderboard(
        @Path("id") id: String,
        @Query("limit") limit: Int = 20
    ): Response<List<LeaderboardEntryDto>>

    // Journal
    @GET("journal")
    suspend fun getJournalEntries(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<JournalEntryDto>

    @GET("journal/{id}")
    suspend fun getJournalEntry(@Path("id") id: String): JournalEntryDto

    // Social Interactions
    @POST("feed/{id}/like")
    suspend fun likePost(
        @Path("id") id: String,
        @Query("reaction") reaction: String? = null
    )

    @DELETE("feed/{id}/like")
    suspend fun unlikePost(@Path("id") id: String)

    @GET("comments")
    suspend fun getCommentsForPost(@Query("postId") postId: String): List<com.ninety5.habitate.data.remote.dto.CommentDto>

    // Account
    @DELETE("users/me")
    suspend fun deleteAccount(): Response<Unit>

    // Generic methods (for sync queue operations)
    @POST("{path}")
    suspend fun create(@Path("path") path: String, @Body payload: okhttp3.RequestBody): okhttp3.ResponseBody

    @PUT("{path}/{id}")
    suspend fun update(@Path("path") path: String, @Path("id") id: String, @Body payload: okhttp3.RequestBody): okhttp3.ResponseBody

    @DELETE("{path}/{id}")
    suspend fun delete(@Path("path") path: String, @Path("id") id: String): okhttp3.ResponseBody
}