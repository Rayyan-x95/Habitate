package com.ninety5.habitate.data.remote.api

import com.ninety5.habitate.data.remote.dto.AuthResponse
import com.ninety5.habitate.data.remote.dto.LeaderboardEntryDto
import com.ninety5.habitate.data.remote.dto.RefreshTokenRequest
import com.ninety5.habitate.data.remote.dto.RegisterRequest
import com.ninety5.habitate.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitateApiService {
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

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<Unit>

    @GET("challenges/{challengeId}/leaderboard")
    suspend fun getLeaderboard(
        @Path("challengeId") challengeId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<LeaderboardEntryDto>>
}
