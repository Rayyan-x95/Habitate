package com.ninety5.habitate.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Represents the current session state of the application.
 */
sealed class SessionState {
    /** No user is authenticated. */
    object Unauthenticated : SessionState()

    /** A user is authenticated and session is valid. */
    data class Authenticated(
        val userId: String,
        val isOnboarded: Boolean,
        val isEmailVerified: Boolean
    ) : SessionState()

    /** Session expired — user was authenticated but token is no longer valid. */
    object SessionExpired : SessionState()
}

/**
 * Centralized session management for the application.
 *
 * Responsibilities:
 * - Observe Firebase AuthStateListener for external sign-outs / token expirations
 * - Expose a single [sessionState] flow for the entire app
 * - Schedule proactive token refresh before expiry
 * - Orchestrate post-login initialization (chat, sync, analytics)
 *
 * Uses [Provider<AuthRepository>] to break circular dependency with Hilt.
 */
@Singleton
class SessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val securePreferences: SecurePreferences,
    private val authRepositoryProvider: Provider<AuthRepository>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tokenRefreshJob: Job? = null

    private val _sessionState = MutableStateFlow<SessionState>(computeInitialState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            _sessionState.value = SessionState.Authenticated(
                userId = user.uid,
                isOnboarded = securePreferences.isOnboarded,
                isEmailVerified = user.isEmailVerified
            )
            Timber.d("SessionManager: Firebase auth state → Authenticated (${user.uid.take(8)}…)")
        } else {
            val wasAuthenticated = _sessionState.value is SessionState.Authenticated
            _sessionState.value = if (wasAuthenticated) {
                Timber.w("SessionManager: Firebase auth state → Session expired (was authenticated)")
                SessionState.SessionExpired
            } else {
                Timber.d("SessionManager: Firebase auth state → Unauthenticated")
                SessionState.Unauthenticated
            }
        }
    }

    /**
     * Start observing auth state changes. Call once from Application.onCreate().
     */
    fun startObserving() {
        firebaseAuth.addAuthStateListener(authStateListener)
        tokenRefreshJob?.cancel()
        tokenRefreshJob = scheduleProactiveTokenRefresh()
        Timber.d("SessionManager: Started observing auth state")
    }

    /**
     * Stop observing. Call on app termination (if needed).
     */
    fun stopObserving() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        scope.coroutineContext.cancelChildren()
        tokenRefreshJob = null
        Timber.d("SessionManager: Stopped observing")
    }

    /**
     * Called after successful login to update session state and trigger
     * post-login initialization.
     */
    fun onLoginSuccess(userId: String) {
        _sessionState.value = SessionState.Authenticated(
            userId = userId,
            isOnboarded = securePreferences.isOnboarded,
            isEmailVerified = firebaseAuth.currentUser?.isEmailVerified ?: false
        )
        // Cancel any existing refresh loop before starting a new one
        tokenRefreshJob?.cancel()
        tokenRefreshJob = scheduleProactiveTokenRefresh()
    }

    /**
     * Called on logout to clear session state.
     */
    fun onLogout() {
        _sessionState.value = SessionState.Unauthenticated
    }

    /**
     * Clear the SessionExpired state (e.g., after user re-authenticates or
     * is redirected to login).
     */
    fun clearExpiredState() {
        if (_sessionState.value is SessionState.SessionExpired) {
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    /**
     * Schedule a token refresh before expiry.
     * Checks token validity every 10 minutes and refreshes if within
     * 2 minutes of expiry.
     */
    private fun scheduleProactiveTokenRefresh(): Job {
        return scope.launch {
            while (true) {
                delay(TOKEN_REFRESH_CHECK_INTERVAL_MS)

                if (_sessionState.value !is SessionState.Authenticated) {
                    continue
                }

                if (securePreferences.isTokenExpiringSoon()) {
                    Timber.d("SessionManager: Token near expiry, refreshing proactively")
                    try {
                        authRepositoryProvider.get().refreshToken()
                    } catch (e: Exception) {
                        Timber.w(e, "SessionManager: Proactive token refresh failed")
                    }
                }
            }
        }
    }

    private fun computeInitialState(): SessionState {
        val firebaseUser = firebaseAuth.currentUser
        val hasValidToken = securePreferences.isTokenValid()
        val userId = firebaseUser?.uid ?: securePreferences.userId

        return when {
            (firebaseUser != null || hasValidToken) && !userId.isNullOrBlank() -> SessionState.Authenticated(
                userId = userId,
                isOnboarded = securePreferences.isOnboarded,
                isEmailVerified = firebaseUser?.isEmailVerified ?: false
            )
            else -> SessionState.Unauthenticated
        }
    }

    companion object {
        /** Check token validity every 10 minutes. */
        private const val TOKEN_REFRESH_CHECK_INTERVAL_MS = 10 * 60 * 1000L
    }
}
