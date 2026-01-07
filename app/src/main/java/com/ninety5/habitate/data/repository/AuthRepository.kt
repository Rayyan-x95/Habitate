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
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.RefreshTokenRequest
import com.ninety5.habitate.data.remote.dto.RegisterRequest
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth state data class
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
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseConfigChecker: FirebaseConfigChecker,
    private val database: HabitateDatabase
) {
    private val refreshMutex = Mutex()

    private val _isAuthenticated = MutableStateFlow(securePreferences.isTokenValid() || firebaseAuth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserId = MutableStateFlow(securePreferences.userId ?: firebaseAuth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    fun getCurrentUserId(): String? {
        return _currentUserId.value
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            Result.failure(e)
        }
    }

    /**
     * Check if email link authentication is available.
     * This should be called before showing the email link sign-in option.
     */
    fun isEmailLinkAuthAvailable(): Boolean {
        return firebaseConfigChecker.isEmailLinkAuthAvailable()
    }

    /**
     * Get user-friendly reason why email link auth is unavailable.
     */
    fun getEmailLinkUnavailableReason(): String {
        return firebaseConfigChecker.getEmailLinkUnavailableReason()
    }

    suspend fun sendSignInLinkToEmail(email: String): Result<Unit> {
        // Pre-check: Ensure email link auth is available
        if (!firebaseConfigChecker.isEmailLinkAuthAvailable()) {
            return Result.failure(
                Exception(firebaseConfigChecker.getEmailLinkUnavailableReason())
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
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e( "sendSignInLinkToEmail failed", e)
            // Map technical errors to user-friendly messages
            val userMessage = when {
                e.message?.contains("CONFIGURATION_NOT_FOUND") == true ||
                e.message?.contains("FDL domain") == true ||
                e.message?.contains("Dynamic Links") == true ->
                    "Sign in with email link is currently unavailable"
                e.message?.contains("invalid-email") == true ->
                    "Please enter a valid email address"
                e.message?.contains("network") == true ->
                    "Network error. Please check your connection"
                else ->
                    "Unable to send sign-in link. Please try again later"
            }
            Result.failure(Exception(userMessage))
        }
    }

    fun isSignInWithEmailLink(emailLink: String): Boolean {
        return firebaseAuth.isSignInWithEmailLink(emailLink)
    }

    suspend fun signInWithEmailLink(email: String, emailLink: String): Result<String> {
        return try {
            val emailToUse = if (email.isNotBlank()) email else securePreferences.pendingEmail ?: ""
            if (emailToUse.isBlank()) {
                return Result.failure(Exception("Please enter your email address to continue"))
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
                    Timber.w( "Token fetch after email link sign-in failed", e)
                }
                
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Unable to sign in. Please try again."))
            }
        } catch (e: Exception) {
            Timber.e( "Email link sign-in failed", e)
            // Map errors to user-friendly messages
            val userMessage = when {
                e.message?.contains("expired", ignoreCase = true) == true ->
                    "This sign-in link has expired. Please request a new one"
                e.message?.contains("invalid", ignoreCase = true) == true ->
                    "This sign-in link is invalid. Please request a new one"
                e.message?.contains("already-used", ignoreCase = true) == true ->
                    "This sign-in link has already been used"
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your connection"
                else ->
                    "Unable to sign in. Please try again"
            }
            Result.failure(Exception(userMessage))
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
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
                    Timber.w( "Backend login exception (non-fatal)", e)
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
                        Timber.w( "Firebase token fallback failed", e)
                    }
                }

                Result.success(user.uid)
            } else {
                Result.failure(Exception("Unable to sign in. Please try again."))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email or password"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email"))
        } catch (e: Exception) {
            Timber.e( "Login failed", e)
            // Map errors to user-friendly messages
            val userMessage = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your connection"
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timed out. Please try again"
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    "Too many attempts. Please wait and try again"
                else ->
                    "Unable to sign in. Please try again"
            }
            Result.failure(Exception(userMessage))
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<String> {
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
                    Timber.w( "Initial backend sync failed, queuing for retry", e)
                    false
                }

                if (!backendSyncSuccess) {
                    // 5. Queue background sync via WorkManager
                    queueBackendSync(email, password, displayName, username, firebaseUser.uid)
                }

                Timber.d( "Registration successful. Backend synced: $backendSyncSuccess")
                Result.success(firebaseUser.uid)
            } else {
                Result.failure(Exception("Registration failed. Please try again."))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account already exists with this email"))
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak. Use at least 8 characters"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Please enter a valid email address"))
        } catch (e: Exception) {
            Timber.e( "Registration failed", e)
            // Map technical errors to user-friendly messages
            val userMessage = when {
                e.message?.contains("network") == true ->
                    "Network error. Please check your connection"
                e.message?.contains("timeout") == true ->
                    "Connection timed out. Please try again"
                else ->
                    "Registration failed. Please try again"
            }
            Result.failure(Exception(userMessage))
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
            Timber.e( "Backend sync exception", e)
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
    fun retryPendingSyncIfNeeded() {
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
    fun hasPendingBackendSync(): Boolean {
        return securePreferences.hasPendingSync() || securePreferences.hasFailedSync()
    }

    suspend fun refreshAccessToken(): Result<String> = refreshMutex.withLock {
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
                    Result.success(body.accessToken)
                } else {
                    // Refresh failed, maybe token expired or revoked
                    Result.failure(Exception("Backend token refresh failed: ${response.code()}"))
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
                        Result.success(token)
                    } else {
                        Result.failure(Exception("Failed to refresh Firebase token"))
                    }
                } else {
                    Result.failure(Exception("No user logged in and no refresh token available"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAccessToken(): String? {
        if (securePreferences.isTokenValid()) {
            return securePreferences.accessToken
        }
        return refreshAccessToken().getOrNull()
    }

    fun getAccessTokenSync(): String? = securePreferences.accessToken

    /**
     * Send password reset email.
     * Uses Firebase Auth's built-in password reset flow.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Please enter your email address"))
            }
            
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d( "Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            // Don't reveal if email exists for security
            Timber.w( "Password reset - user not found: $email")
            Result.success(Unit) // Pretend success for security
        } catch (e: Exception) {
            Timber.e( "Password reset failed", e)
            val userMessage = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your connection"
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "Please enter a valid email address"
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    "Too many requests. Please wait and try again"
                else ->
                    "Unable to send reset email. Please try again"
            }
            Result.failure(Exception(userMessage))
        }
    }

    suspend fun logout() {
        try {
            // Sign out from Firebase
            firebaseAuth.signOut()

            // Try to revoke tokens on server (best effort)
            securePreferences.accessToken?.let { token ->
                try {
                    apiService.logout()
                } catch (e: Exception) {
                    Timber.w( "Server logout failed", e)
                }
            }
        } catch (e: Exception) {
            Timber.w( "Logout error", e)
        } finally {
            // Clear local data to ensure privacy
            database.clearAllTables()
            securePreferences.clearAuth()
            _isAuthenticated.value = false
            _currentUserId.value = null
        }
    }

    fun isOnboarded(): Boolean = securePreferences.isOnboarded

    fun setOnboarded(onboarded: Boolean) {
        securePreferences.isOnboarded = onboarded
    }

    fun isBiometricEnabled(): Boolean = securePreferences.biometricEnabled

    fun setBiometricEnabled(enabled: Boolean) {
        securePreferences.biometricEnabled = enabled
    }

    /**
     * Send email verification to the current user.
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            user.sendEmailVerification().await()
            Timber.d( "Verification email sent to ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e( "Failed to send verification email", e)
            val message = when {
                e.message?.contains("too-many-requests") == true -> "Too many requests. Please wait."
                else -> "Failed to send verification email."
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * Check if the current user's email is verified.
     */
    fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    /**
     * Reload user data from Firebase (to refresh email verification status).
     */
    suspend fun reloadUser(): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))
        return try {
            user.reload().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            // 1. Delete from backend API (best effort - don't block on failure)
            try {
                apiService.deleteAccount()
            } catch (e: Exception) {
                // Log but continue - Firebase deletion is primary
                Timber.w( "Backend account deletion failed", e)
            }
            
            // 2. Delete from Firebase (PRIMARY - this is the source of truth)
            firebaseAuth.currentUser?.delete()?.await()
            
            // 3. Clear local DB
            database.clearAllTables()
            
            // 4. Clear preferences
            securePreferences.clearAuth()
            
            // 5. Update state
            _isAuthenticated.value = false
            _currentUserId.value = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
