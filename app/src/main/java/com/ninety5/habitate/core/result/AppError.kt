package com.ninety5.habitate.core.result

import java.io.IOException

/**
 * Typed error hierarchy for the Habitate application.
 *
 * Provides structured error information that can be displayed to users
 * or logged for debugging without leaking implementation details.
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    // ── Network Errors ─────────────────────────────────────────────────
    data class Network(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Timeout(
        override val message: String = "Request timed out",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class NoConnection(
        override val message: String = "No internet connection",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // ── API Errors ─────────────────────────────────────────────────────
    data class Unauthorized(
        override val message: String = "Session expired. Please log in again.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Forbidden(
        override val message: String = "You don't have permission for this action",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class NotFound(
        override val message: String = "The requested resource was not found",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Conflict(
        override val message: String = "A conflict occurred. Please try again.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class RateLimited(
        override val message: String = "Too many requests. Please wait a moment.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Server(
        override val message: String = "Server error occurred",
        val code: Int = 500,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // ── Local Errors ───────────────────────────────────────────────────
    data class Database(
        override val message: String = "Database error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Validation(
        override val message: String,
        val field: String? = null,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // ── Generic ────────────────────────────────────────────────────────
    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * Returns a user-friendly message suitable for display in snackbars or error states.
     */
    val userMessage: String
        get() = message

    companion object {
        /**
         * Converts a generic [Throwable] into a typed [AppError].
         */
        fun from(throwable: Throwable): AppError = when (throwable) {
            is java.net.SocketTimeoutException -> Timeout(cause = throwable)
            is IOException -> NoConnection(cause = throwable)
            is retrofit2.HttpException -> fromHttpCode(throwable.code(), throwable)
            else -> Unknown(
                message = throwable.message ?: "An unexpected error occurred",
                cause = throwable
            )
        }

        /**
         * Maps an HTTP status code to the appropriate [AppError].
         */
        fun fromHttpCode(code: Int, cause: Throwable? = null): AppError = when (code) {
            401 -> Unauthorized(cause = cause)
            403 -> Forbidden(cause = cause)
            404 -> NotFound(cause = cause)
            409 -> Conflict(cause = cause)
            429 -> RateLimited(cause = cause)
            in 500..599 -> Server(code = code, cause = cause)
            else -> Unknown(message = "HTTP error $code", cause = cause)
        }
    }
}
