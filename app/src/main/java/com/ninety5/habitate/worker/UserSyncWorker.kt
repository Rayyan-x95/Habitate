package com.ninety5.habitate.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.data.remote.ApiService
import com.ninety5.habitate.data.remote.dto.RegisterRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Background worker for syncing user registration with backend.
 * 
 * CRITICAL: Firebase Auth success is FINAL. This worker ensures backend
 * sync is eventually consistent without blocking the user experience.
 * 
 * Retry Strategy:
 * - Exponential backoff with max 3 retries
 * - On success: User is fully synced
 * - On failure: User can still use app with Firebase auth
 */
@HiltWorker
class UserSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SYNC_TYPE = "syncType"
        const val KEY_USER_ID = "userId"
        
        const val SYNC_TYPE_REGISTER = "register"
        const val SYNC_TYPE_LOGIN = "login"
        
        const val MAX_RETRIES = 3
    }

    override suspend fun doWork(): Result {
        val syncType = inputData.getString(KEY_SYNC_TYPE) ?: SYNC_TYPE_REGISTER
        val userId = inputData.getString(KEY_USER_ID)
        
        // SECURITY: Read sensitive credentials from encrypted SecurePreferences, NOT from WorkManager inputData
        val email = securePreferences.pendingSyncEmail ?: run {
            Timber.e("UserSyncWorker: No pending sync email found in SecurePreferences")
            return Result.failure()
        }
        val password = securePreferences.pendingSyncPassword ?: run {
            Timber.e("UserSyncWorker: No pending sync password found in SecurePreferences")
            return Result.failure()
        }
        val displayName = securePreferences.pendingSyncDisplayName
        val username = securePreferences.pendingSyncUsername
        
        Timber.d("UserSyncWorker: Starting $syncType sync for user $userId")
        
        return try {
            when (syncType) {
                SYNC_TYPE_REGISTER -> {
                    if (displayName == null || username == null) {
                        Timber.e("UserSyncWorker: Missing required fields for registration")
                        return Result.failure()
                    }
                    performRegistrationSync(email, password, displayName, username)
                }
                SYNC_TYPE_LOGIN -> {
                    performLoginSync(email, password)
                }
                else -> {
                    Timber.e("UserSyncWorker: Unknown sync type: $syncType")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "UserSyncWorker: Sync failed, attempt ${runAttemptCount}")
            handleRetry()
        }
    }

    private suspend fun performRegistrationSync(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result {
        // Step 1: Register with backend
        val registerResponse = apiService.register(
            RegisterRequest(email, password, displayName, username)
        )
        
        if (!registerResponse.isSuccessful) {
            val errorCode = registerResponse.code()
            Timber.w("UserSyncWorker: Backend registration failed with code $errorCode")
            
            // If user already exists on backend, that's OK - proceed to login
            if (errorCode == 409) {
                Timber.d("UserSyncWorker: User already exists on backend, proceeding to login")
            } else {
                throw Exception("Backend registration failed: $errorCode")
            }
        } else {
            Timber.d("UserSyncWorker: Backend registration successful")
        }
        
        // Step 2: Login to get tokens
        return performLoginSync(email, password)
    }

    private suspend fun performLoginSync(email: String, password: String): Result {
        val loginResponse = apiService.login(email, password)
        
        val authResponse = loginResponse.body()
        if (loginResponse.isSuccessful && authResponse != null) {
            securePreferences.saveTokens(
                access = authResponse.accessToken,
                refresh = authResponse.refreshToken,
                expiresInMs = 15 * 60 * 1000L // 15 minutes
            )
            Timber.d("UserSyncWorker: Backend login successful, tokens saved")
            
            // Mark sync as complete
            securePreferences.setPendingSyncComplete()
            
            return Result.success(
                workDataOf("synced" to true)
            )
        } else {
            throw Exception("Backend login failed: ${loginResponse.code()}")
        }
    }

    private fun handleRetry(): Result {
        return if (runAttemptCount < MAX_RETRIES) {
            Timber.d("UserSyncWorker: Scheduling retry ${runAttemptCount + 1}/$MAX_RETRIES")
            Result.retry()
        } else {
            Timber.e("UserSyncWorker: Max retries exceeded, marking as failed")
            // Don't fail catastrophically - user can still use app with Firebase auth
            // The sync can be retried on next app launch or manual trigger
            securePreferences.setPendingSyncFailed()
            Result.failure(
                workDataOf("error" to "Backend sync failed after $MAX_RETRIES attempts")
            )
        }
    }
}
