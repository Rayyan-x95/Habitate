package com.ninety5.habitate.domain.usecase

import com.ninety5.habitate.core.result.AppResult

/**
 * Base class for Use Cases (Interactors) in Clean Architecture.
 *
 * A Use Case encapsulates a single piece of business logic. ViewModels should
 * call Use Cases instead of repositories directly. This enables:
 * - Reuse of business logic across ViewModels
 * - Easier unit testing (mock Use Cases, not repositories)
 * - Single Responsibility per class
 *
 * ## Conventions
 * - Each Use Case has a single public method: [invoke] (called via `operator fun`)
 * - Use Cases return [AppResult] for consistent error handling
 * - Use Cases are injected via Hilt constructor injection
 *
 * ## Example
 * ```kotlin
 * class GetUserProfileUseCase @Inject constructor(
 *     private val userRepository: UserRepository
 * ) : UseCase<String, UserProfile>() {
 *     override suspend fun execute(params: String): AppResult<UserProfile> =
 *         safeCall { userRepository.getUserProfile(params) }
 * }
 *
 * // In ViewModel:
 * val result = getUserProfile("user-123")
 * ```
 */
abstract class UseCase<in P, out R> {

    suspend operator fun invoke(params: P): AppResult<R> = execute(params)

    protected abstract suspend fun execute(params: P): AppResult<R>
}

/**
 * A Use Case that takes no parameters.
 *
 * ## Example
 * ```kotlin
 * class GetCurrentUserUseCase @Inject constructor(
 *     private val authRepository: AuthRepository
 * ) : NoParamUseCase<User>() {
 *     override suspend fun execute(): AppResult<User> =
 *         safeCall { authRepository.getCurrentUser() }
 * }
 * ```
 */
abstract class NoParamUseCase<out R> {

    suspend operator fun invoke(): AppResult<R> = execute()

    protected abstract suspend fun execute(): AppResult<R>
}

/**
 * A Use Case that returns a Flow for observable data.
 *
 * ## Example
 * ```kotlin
 * class ObserveFeedUseCase @Inject constructor(
 *     private val feedRepository: FeedRepository
 * ) : FlowUseCase<Unit, List<Post>>() {
 *     override fun execute(params: Unit): Flow<AppResult<List<Post>>> =
 *         feedRepository.observeFeed().map { AppResult.Success(it) }
 * }
 * ```
 */
abstract class FlowUseCase<in P, out R> {

    operator fun invoke(params: P): kotlinx.coroutines.flow.Flow<AppResult<R>> = execute(params)

    protected abstract fun execute(params: P): kotlinx.coroutines.flow.Flow<AppResult<R>>
}
