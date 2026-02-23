package com.ninety5.habitate.core.result

/**
 * A unified Result wrapper for the Habitate application.
 *
 * Replaces ad-hoc `kotlin.Result` usage with a richer sealed class that
 * supports loading state, typed errors, and fluent transformations.
 *
 * Usage:
 * ```kotlin
 * suspend fun getUser(id: String): AppResult<User> =
 *     try {
 *         val user = api.getUser(id)
 *         AppResult.Success(user)
 *     } catch (e: Exception) {
 *         AppResult.Error(AppError.Network(e.message))
 *     }
 * ```
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if this is [Success], or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if this is [Success], or [default] otherwise.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Returns the error if this is [Error], or null otherwise.
     */
    fun errorOrNull(): AppError? = when (this) {
        is Error -> error
        else -> null
    }

    /**
     * Maps the success data using [transform].
     */
    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * FlatMaps the success data using [transform].
     */
    inline fun <R> flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Executes [action] if this is [Success].
     */
    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes [action] if this is [Error].
     */
    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }
}

/**
 * Converts a [kotlin.Result] to [AppResult].
 */
fun <T> Result<T>.toAppResult(): AppResult<T> =
    fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(AppError.from(it)) }
    )

/**
 * Wraps a suspending call in an [AppResult], catching exceptions.
 */
suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error(AppError.from(e))
    }
