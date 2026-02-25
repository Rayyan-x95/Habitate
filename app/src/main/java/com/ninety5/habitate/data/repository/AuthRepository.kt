package com.ninety5.habitate.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ninety5.habitate.core.FirebaseConfigChecker
import com.ninety5.habitate.core.result.AppError
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.RefreshTokenRequest
import com.ninety5.habitate.data.remote.dto.RegisterRequest
import com.ninety5.habitate.domain.repository.AuthRepository
import com.ninety5.habitate.worker.UserSyncWorker
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.ninety5.habitate.data.local.HabitateDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth state data class for internal use.
 */
data class AuthState(
    val isLoggedIn: Boolean,
    val isOnboarded: Boolean,
    val isEmailVerified: Boolean
)

/**
 * Repository for authentication operations.
 * Handles login, registration, token refresh, and session management.
 * 
 * CRITICAL DESIGN PRINCIPLES:
 * 1. Firebase Auth success = Account created (FINAL)
 * 2. Backend sync is eventually consistent (never blocks user)
 * 3. Config errors never surface as scary runtime errors
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseConfigChecker: FirebaseConfigChecker,
    private val database: HabitateDatabase
) : AuthRepository {
    private val refreshMutex = Mutex()

    private val _isAuthenticated = MutableStateFlow(securePreferences.isTokenValid() || firebaseAuth.currentUser != null)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserId = MutableStateFlow(securePreferences.userId ?: firebaseAuth.currentUser?.uid)
    override val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    override fun getCurrentUserId(): String? {
        return _currentUserId.value
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Google Sign-In failed: User is null")

            // Sync with backend
            val synced = syncWithBackend(
                email = user.email ?: "",
                password = "", // No password for Google Sign-In
                displayName = user.displayName ?: "User",
                username = user.email?.substringBefore("@") ?: "user_${System.currentTimeMillis()}"
            )

            if (!synced) {
                queueBackendSync(
                    email = user.email ?: "",
                    password = "",
                    displayName = user.displayName ?: "User",
                    username = user.email?.substringBefore("@") ?: "user_${System.currentTimeMillis()}",
                    userId = user.uid
                )
            }

            _currentUserId.value = user.uid
            _isAuthenticated.value = true
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            AppResult.Error(AppError.Unknown(e.message ?: "Google Sign-In failed", e))
        }
    }

    /**
     * Check if email link authentication is available.
     * This should be called before showing the email link sign-in option.
     */
    override fun isEmailLinkAuthAvailable(): Boolean {
        return firebaseConfigChecker.isEmailLinkAuthAvailable()
    }

    /**
     * Get user-friendly reason why email link auth is unavailable.
     */
    override fun getEmailLinkUnavailableReason(): String {
        return firebaseConfigChecker.getEmailLinkUnavailableReason()
    }

    override suspend fun sendSignInLinkToEmail(email: String): AppResult<Unit> {
        // Pre-check: Ensure email link auth is available
        if (!firebaseConfigChecker.isEmailLinkAuthAvailable()) {
            return AppResult.Error(
                AppError.Unknown(firebaseConfigChecker.getEmailLinkUnavailableReason())
            )
        }

        return try {
            // Use a properly configured ActionCodeSettings
            // IMPORTANT: The URL must be an authorized domain in Firebase Console
            // AND if using Dynamic Links (.page.link), Dynamic Links must be configured
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                // Use the Firebase Hosting URL (doesn't require Dynamic Links)
                // This should be updated to your actual Firebase Hosting domain
                .setUrl("https://habitate-eafeb.firebaseapp.com/__/auth/action")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.ninety5.habitate",
                    true,  // installIfNotAvailable
                    "29"   // minimumVersion (matches minSdk)
                )
                .build()

            firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings).await()
            securePreferences.pendingEmail = email
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "sendSignInLinkToEmail failed")
            // Map technical errors to user-friendly messages
            when {
                e.message?.contains("CONFIGURATION_NOT_FOUND") == true ||
                e.message?.contains("FDL domain") == true ||
                e.message?.contains("Dynamic Links") == true ->
                    AppResult.Error(AppError.Unknown("Sign in with email link is currently unavailable", e))
                e.message?.contains("invalid-email") == true ->
                    AppResult.Error(AppError.Validation("Please enter a valid email address", "email"))
                e.message?.contains("network") == true ->
                    AppResult.Error(AppError.NoConnection("Network error. Please check your connection", e))
                else ->
                    AppResult.Error(AppError.Unknown("Unable to send sign-in link. Please try again later", e))
            }
        }
    }

    override fun isSignInWithEmailLink(emailLink: String): Boolean {
        return firebaseAuth.isSignInWithEmailLink(emailLink)
    }

    override suspend fun signInWithEmailLink(email: String, emailLink: String): AppResult<String> {
        return try {
            val emailToUse = if (email.isNotBlank()) email else securePreferences.pendingEmail ?: ""
            if (emailToUse.isBlank()) {
                return AppResult.Error(AppError.Validation("Please enter your email address to continue", "email"))
            }

            val authResult = firebaseAuth.signInWithEmailLink(emailToUse, emailLink).await()
            val user = authResult.user
            if (user != null) {
                _isAuthenticated.value = true
                _currentUserId.value = user.uid
                securePreferences.userId = user.uid
                securePreferences.pendingEmail = null // Clear pending email
                
                // Attempt to get Firebase token for API calls
                try {
                    val tokenResult = user.getIdToken(true).await()
                    tokenResult.token?.let { token ->
                        securePreferences.saveTokens(
                            access = token,
                            refresh = "firebase_refresh_managed",
                            expiresInMs = 3600 * 1000L
                        )
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Token fetch after email link sign-in failed")
                }
                
                AppResult.Success(user.uid)
            } else {
                AppResult.Error(AppError.Unknown("Unable to sign in. Please try again."))
            }
        } catch (e: Exception) {
            Timber.e(e, "Email link sign-in failed")
            // Map errors to user-friendly messages
            when {
                e.message?.contains("expired", ignoreCase = true) == true ->
                    AppResult.Error(AppError.Unknown("This sign-in link has expired. Please request a new one", e))
                e.message?.contains("invalid", ignoreCase = true) == true ->
                    AppResult.Error(AppError.Unknown("This sign-in link is invalid. Please request a new one", e))
                e.message?.contains("already-used", ignoreCase = true) == true ->
                    AppResult.Error(AppError.Conflict("This sign-in link has already been used", e))
                e.message?.contains("network", ignoreCase = true) == true ->
                    AppResult.Error(AppError.NoConnection("Network error. Please check your connection", e))
                else ->
                    AppResult.Error(AppError.Unknown("Unable to sign in. Please try again", e))
            }
        }
    }

    override suspend fun login(email: String, password: String): AppResult<String> {
        return try {
            // 1. Login with Firebase (PRIMARY - success means user is authenticated)
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // 2. Mark user as authenticated IMMEDIATELY
                _isAuthenticated.value = true
                _currentUserId.value = user.uid
                securePreferences.userId = user.uid

                // 3. Attempt backend token sync (non-blocking on failure)
                val backendSuccess = try {
                    val response = apiService.login(email, password)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        securePreferences.saveTokens(
                            access = body.accessToken,
                            refresh = body.refreshToken,
                            expiresInMs = 15 * 60 * 1000L // 15 minutes
                        )
                        Timber.d( "Backend login successful")
                        true
                    } else {
                        Timber.w( "Backend login failed: ${response.code()}")
                        false
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Backend login exception (non-fatal)")
                    false
                }

                // 4. If backend failed, use Firebase token as fallback
                if (!backendSuccess) {
                    try {
                        val tokenResult = user.getIdToken(true).await()
                        tokenResult.token?.let { token ->
                            securePreferences.saveTokens(
                                access = token,
                                refresh = "firebase_refresh_managed",
                                expiresInMs = 3600 * 1000L // 1 hour
                            )
                            Timber.d( "Using Firebase token as fallback")
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Firebase token fallback failed")
                    }
                }

                AppResult.Success(user.uid)
            } else {
                AppResult.Error(AppError.Unknown("Unable to sign in. Please try again."))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.w("Login failed: %s", e.errorCode)
            AppResult.Error(AppError.Unauthorized("Invalid email or password", e))
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.w("Login failed: %s", e.errorCode)
            AppResult.Error(AppError.NotFound("No account found with this email", e))
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            // Map errors to user-friendly messages
            when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    AppResult.Error(AppError.NoConnection("Network error. Please check your connection", e))
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    AppResult.Error(AppError.Timeout("Connection timed out. Please try again", e))
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    AppResult.Error(AppError.RateLimited("Too many attempts. Please wait and try again", e))
                else ->
                    AppResult.Error(AppError.Unknown("Unable to sign in. Please try again", e))
            }
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): AppResult<String> {
        return try {
            // 1. Create user with Firebase Auth (THIS IS FINAL - never rollback)
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 2. Update user profile with display name
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    this.displayName = displayName
                }
                firebaseUser.updateProfile(profileUpdates).await()

                // 3. Store auth info IMMEDIATELY (user is logged in)
                _isAuthenticated.value = true
                _currentUserId.value = firebaseUser.uid
                securePreferences.userId = firebaseUser.uid

                // 4. Attempt backend sync (non-blocking)
                val backendSyncSuccess = try {
                    syncWithBackend(email, password, displayName, username)
                } catch (e: Exception) {
                    Timber.w(e, "Initial backend sync failed, queuing for retry")
                    false
                }

                if (!backendSyncSuccess) {
                    // 5. Queue background sync via WorkManager
                    queueBackendSync(email, password, displayName, username, firebaseUser.uid)
                }

                Timber.d( "Registration successful. Backend synced: $backendSyncSuccess")
                AppResult.Success(firebaseUser.uid)
            } else {
                AppResult.Error(AppError.Unknown("Registration failed. Please try again."))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Timber.w("Registration failed: User collision")
            AppResult.Error(AppError.Conflict("An account already exists with this email", e))
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            Timber.w("Registration failed: Weak password")
            AppResult.Error(AppError.Validation("Password is too weak. Use at least 8 characters", "password"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.w("Registration failed: Invalid credentials")
            AppResult.Error(AppError.Validation("Please enter a valid email address", "email"))
        } catch (e: Exception) {
            Timber.e(e, "Registration failed")
            // Map technical errors to user-friendly messages
            when {
                e.message?.contains("network") == true ->
                    AppResult.Error(AppError.NoConnection("Network error. Please check your connection", e))
                e.message?.contains("timeout") == true ->
                    AppResult.Error(AppError.Timeout("Connection timed out. Please try again", e))
                else ->
                    AppResult.Error(AppError.Unknown("Registration failed. Please try again", e))
            }
        }
    }

    /**
     * Attempt synchronous backend sync.
     * Returns true if successful, false if needs retry.
     */
    private suspend fun syncWithBackend(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Boolean {
        return try {
            // Register with backend
            val registerResponse = apiService.register(
                RegisterRequest(email, password, displayName, username)
            )
            
            if (!registerResponse.isSuccessful && registerResponse.code() != 409) {
                Timber.w( "Backend registration failed: ${registerResponse.code()}")
                return false
            }

            // Login to get tokens
            val loginResponse = apiService.login(email, password)
            val loginBody = loginResponse.body()
            if (loginResponse.isSuccessful && loginBody != null) {
                securePreferences.saveTokens(
                    access = loginBody.accessToken,
                    refresh = loginBody.refreshToken,
                    expiresInMs = 15 * 60 * 1000L
                )
                securePreferences.setPendingSyncComplete()
                Timber.d( "Backend sync successful")
                true
            } else {
                Timber.w( "Backend login failed: ${loginResponse.code()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Backend sync exception")
            false
        }
    }

    /**
     * Queue backend sync for background processing.
     * Uses WorkManager with exponential backoff.
     */
    private fun queueBackendSync(
        email: String,
        password: String,
        displayName: String,
        username: String,
        userId: String
    ) {
        // Store pending sync data in ENCRYPTED SecurePreferences
        // SECURITY: Credentials are stored here, not in WorkManager inputData
        securePreferences.setPendingSync(email, password, displayName, username)

        // Create work request with constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // SECURITY: Only pass non-sensitive identifiers via WorkManager
        // Sensitive data (email, password) is read from encrypted SecurePreferences by the worker
        val syncRequest = OneTimeWorkRequestBuilder<UserSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .setInputData(
                workDataOf(
                    UserSyncWorker.KEY_SYNC_TYPE to UserSyncWorker.SYNC_TYPE_REGISTER,
                    UserSyncWorker.KEY_USER_ID to userId
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "user_sync_$userId",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Timber.d( "Backend sync queued for background processing")
    }

    /**
     * Check if there's a pending backend sync and retry if needed.
     * Called on app launch.
     */
    override fun retryPendingSyncIfNeeded() {
        if (securePreferences.hasPendingSync() || securePreferences.hasFailedSync()) {
            val email = securePreferences.pendingSyncEmail ?: return
            val password = securePreferences.pendingSyncPassword ?: return
            val displayName = securePreferences.pendingSyncDisplayName ?: return
            val username = securePreferences.pendingSyncUsername ?: return
            val userId = securePreferences.userId ?: return

            Timber.d( "Retrying pending backend sync")
            queueBackendSync(email, password, displayName, username, userId)
        }
    }

    /**
     * Check if backend sync is pending/failed.
     * Can be used to show subtle UI indicator.
     */
    override fun hasPendingBackendSync(): Boolean {
        return securePreferences.hasPendingSync() || securePreferences.hasFailedSync()
    }

    override suspend fun refreshToken(): AppResult<String> = refreshMutex.withLock {
        return try {
            val refreshToken = securePreferences.refreshToken
            if (!refreshToken.isNullOrBlank() && refreshToken != "firebase_refresh_managed") {
                // Use Backend Refresh Token
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    securePreferences.saveTokens(
                        access = body.accessToken,
                        refresh = body.refreshToken,
                        expiresInMs = 15 * 60 * 1000L
                    )
                    AppResult.Success(body.accessToken)
                } else {
                    // Refresh failed, maybe token expired or revoked
                    AppResult.Error(AppError.Unauthorized("Backend token refresh failed: ${response.code()}"))
                }
            } else {
                // Fallback to Firebase Refresh (if we are in legacy mode or just using Firebase)
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val tokenResult = user.getIdToken(true).await()
                    val token = tokenResult.token
                    if (token != null) {
                        securePreferences.saveTokens(
                            access = token,
                            refresh = "firebase_refresh_managed",
                            expiresInMs = 3600 * 1000L
                        )
                        AppResult.Success(token)
                    } else {
                        AppResult.Error(AppError.Unknown("Failed to refresh Firebase token"))
                    }
                } else {
                    AppResult.Error(AppError.Unauthorized("No user logged in and no refresh token available"))
                }
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Token refresh failed", e))
        }
    }

    override suspend fun getAccessToken(): String? {
        if (securePreferences.isTokenValid()) {
            return securePreferences.accessToken
        }
        return when (val result = refreshToken()) {
            is AppResult.Success -> result.data
            else -> null
        }
    }

    override fun getAccessTokenSync(): String? = securePreferences.accessToken

    /**
     * Send password reset email.
     * Uses Firebase Auth's built-in password reset flow.
     */
    override suspend fun sendPasswordResetEmail(email: String): AppResult<Unit> {
        return try {
            if (email.isBlank()) {
                return AppResult.Error(AppError.Validation("Please enter your email address", "email"))
            }
            
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d("Password reset email requested")
            AppResult.Success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            // Don't reveal if email exists for security
            Timber.w("Password reset requested for non-existing user: %s", e.errorCode)
            AppResult.Success(Unit) // Pretend success for security
        } catch (e: Exception) {
            Timber.e(e, "Password reset failed")
            when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    AppResult.Error(AppError.NoConnection("Network error. Please check your connection", e))
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    AppResult.Error(AppError.Validation("Please enter a valid email address", "email"))
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    AppResult.Error(AppError.RateLimited("Too many requests. Please wait and try again", e))
                else ->
                    AppResult.Error(AppError.Unknown("Unable to send reset email. Please try again", e))
            }
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        return try {
            // Sign out from Firebase
            firebaseAuth.signOut()

            // Try to revoke tokens on server (best effort)
            securePreferences.accessToken?.let { token ->
                try {
                    apiService.logout()
                } catch (e: Exception) {
                    Timber.w(e, "Server logout failed")
                }
            }

            // Clear local data to ensure privacy
            withContext(Dispatchers.IO) { database.clearAllTables() }
            securePreferences.clearAuth()
            _isAuthenticated.value = false
            _currentUserId.value = null

            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.w(e, "Logout error")
            // Still clear local state even on error
            withContext(Dispatchers.IO) { database.clearAllTables() }
            securePreferences.clearAuth()
            _isAuthenticated.value = false
            _currentUserId.value = null
            AppResult.Success(Unit) // logout always "succeeds" locally
        }
    }

    override fun isLoggedIn(): Boolean = _isAuthenticated.value

    override fun isOnboarded(): Boolean = securePreferences.isOnboarded

    override fun setOnboarded(onboarded: Boolean) {
        securePreferences.isOnboarded = onboarded
    }

    override fun isBiometricEnabled(): Boolean = securePreferences.biometricEnabled

    override fun setBiometricEnabled(enabled: Boolean) {
        securePreferences.biometricEnabled = enabled
    }

    /**
     * Send email verification to the current user.
     */
    override suspend fun sendEmailVerification(): AppResult<Unit> {
        val user = firebaseAuth.currentUser ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))
        
        return try {
            user.sendEmailVerification().await()
            Timber.d("Verification email sent")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send verification email")
            when {
                e.message?.contains("too-many-requests") == true ->
                    AppResult.Error(AppError.RateLimited("Too many requests. Please wait.", e))
                else ->
                    AppResult.Error(AppError.Unknown("Failed to send verification email.", e))
            }
        }
    }

    /**
     * Check if the current user's email is verified.
     */
    override fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    /**
     * Reload user data from Firebase (to refresh email verification status).
     */
    override suspend fun reloadUser(): AppResult<Unit> {
        val user = firebaseAuth.currentUser ?: return AppResult.Error(AppError.Unauthorized("User not logged in"))
        return try {
            user.reload().await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Failed to reload user", e))
        }
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        return try {
            // 1. Delete from backend API (best effort - don't block on failure)
            try {
                apiService.deleteAccount()
            } catch (e: Exception) {
                // Log but continue - Firebase deletion is primary
                Timber.w(e, "Backend account deletion failed")
            }
            
            // 2. Delete from Firebase (PRIMARY - this is the source of truth)
            firebaseAuth.currentUser?.delete()?.await()
            
            // 3. Clear local DB
            withContext(Dispatchers.IO) { database.clearAllTables() }
            
            // 4. Clear preferences
            securePreferences.clearAuth()
            
            // 5. Update state
            _isAuthenticated.value = false
            _currentUserId.value = null
            
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Failed to delete account", e))
        }
    }
    
    /**
     * Get current authentication state
     */
    fun getAuthState(): AuthState {
        return AuthState(
            isLoggedIn = _isAuthenticated.value,
            isOnboarded = securePreferences.isOnboarded,
            isEmailVerified = firebaseAuth.currentUser?.isEmailVerified ?: false
        )
    }
}
