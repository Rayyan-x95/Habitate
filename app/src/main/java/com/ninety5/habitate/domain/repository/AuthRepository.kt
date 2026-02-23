package com.ninety5.habitate.domain.repository

import com.ninety5.habitate.core.result.AppResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain contract for authentication operations.
 *
 * Implementation in data layer handles Firebase Auth + backend JWT sync.
 * All operations return [AppResult] for structured error handling.
 *
 * Design principles:
 * 1. Firebase Auth success = Account created (FINAL)
 * 2. Backend sync is eventually consistent (never blocks user)
 * 3. Errors are typed via [AppResult] / [com.ninety5.habitate.core.result.AppError]
 */
interface AuthRepository {

    // ── Reactive State ──────────────────────────────────────────────────────

    /** Observable auth state — true when the user has a valid session. */
    val isAuthenticated: StateFlow<Boolean>

    /** Observable current user ID (null when logged out). */
    val currentUserId: StateFlow<String?>

    // ── Auth Operations ─────────────────────────────────────────────────────

    /** Email/password login. Returns user ID on success. */
    suspend fun login(email: String, password: String): AppResult<String>

    /** Register a new account. Returns user ID on success. */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): AppResult<String>

    /** Google Sign-In with Firebase ID token. */
    suspend fun loginWithGoogle(idToken: String): AppResult<Unit>

    /** Sign out, clear local data, revoke server tokens (best effort). */
    suspend fun logout(): AppResult<Unit>

    /** Permanently delete account from Firebase + backend + local. */
    suspend fun deleteAccount(): AppResult<Unit>

    // ── Token Management ────────────────────────────────────────────────────

    /** Refresh the access token (backend JWT or Firebase ID token fallback). */
    suspend fun refreshToken(): AppResult<String>

    /** Get a valid access token, refreshing if expired. */
    suspend fun getAccessToken(): String?

    /** Get access token synchronously (may be expired). */
    fun getAccessTokenSync(): String?

    // ── Email Link Auth ─────────────────────────────────────────────────────

    /** Check if email-link (passwordless) auth is available. */
    fun isEmailLinkAuthAvailable(): Boolean

    /** Get user-friendly reason why email link auth is unavailable. */
    fun getEmailLinkUnavailableReason(): String

    /** Send a sign-in link to the given email address. */
    suspend fun sendSignInLinkToEmail(email: String): AppResult<Unit>

    /** Check if a given deep link is a sign-in email link. */
    fun isSignInWithEmailLink(emailLink: String): Boolean

    /** Complete sign-in with email link. Returns user ID. */
    suspend fun signInWithEmailLink(email: String, emailLink: String): AppResult<String>

    // ── Email Verification ──────────────────────────────────────────────────

    /** Send verification email to the current user. */
    suspend fun sendEmailVerification(): AppResult<Unit>

    /** Check if current user's email is verified (locally cached). */
    fun isEmailVerified(): Boolean

    /** Reload user data from Firebase to refresh verification status. */
    suspend fun reloadUser(): AppResult<Unit>

    // ── Password Reset ──────────────────────────────────────────────────────

    /** Send password reset email (security: always returns success). */
    suspend fun sendPasswordResetEmail(email: String): AppResult<Unit>

    // ── Session Queries ─────────────────────────────────────────────────────

    /** Synchronous check: is the user currently logged in? */
    fun isLoggedIn(): Boolean

    /** Synchronous read of the current user ID. */
    fun getCurrentUserId(): String?

    /** Whether onboarding has been completed. */
    fun isOnboarded(): Boolean

    /** Mark onboarding as complete. */
    fun setOnboarded(onboarded: Boolean)

    /** Whether biometric unlock is enabled. */
    fun isBiometricEnabled(): Boolean

    /** Enable/disable biometric unlock. */
    fun setBiometricEnabled(enabled: Boolean)

    // ── Backend Sync ────────────────────────────────────────────────────────

    /** Retry any pending backend registration sync. */
    fun retryPendingSyncIfNeeded()

    /** Check if backend sync is pending or failed. */
    fun hasPendingBackendSync(): Boolean
}

/**
 * Represents an authenticated session.
 * Used internally for structured token passing.
 */
data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String
)
