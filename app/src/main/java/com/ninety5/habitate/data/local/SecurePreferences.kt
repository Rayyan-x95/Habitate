package com.ninety5.habitate.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var userId: String?
        get() = sharedPreferences.getString("user_id", null)
        set(value) = sharedPreferences.edit().putString("user_id", value).apply()

    var pendingEmail: String?
        get() = sharedPreferences.getString("pending_email", null)
        set(value) = sharedPreferences.edit().putString("pending_email", value).apply()

    var accessToken: String?
        get() = sharedPreferences.getString("access_token", null)
        private set(value) = sharedPreferences.edit().putString("access_token", value).apply()

    var refreshToken: String?
        get() = sharedPreferences.getString("refresh_token", null)
        private set(value) = sharedPreferences.edit().putString("refresh_token", value).apply()

    private var tokenExpiry: Long
        get() = sharedPreferences.getLong("token_expiry", 0)
        set(value) = sharedPreferences.edit().putLong("token_expiry", value).apply()

    fun saveTokens(access: String, refresh: String, expiresInMs: Long) {
        sharedPreferences.edit()
            .putString("access_token", access)
            .putString("refresh_token", refresh)
            .putLong("token_expiry", System.currentTimeMillis() + expiresInMs)
            .apply()
    }

    fun isTokenValid(): Boolean {
        return accessToken != null && System.currentTimeMillis() < tokenExpiry
    }

    var isOnboarded: Boolean
        get() = sharedPreferences.getBoolean("is_onboarded", false)
        set(value) = sharedPreferences.edit().putBoolean("is_onboarded", value).apply()

    var biometricEnabled: Boolean
        get() = sharedPreferences.getBoolean("biometric_enabled", false)
        set(value) = sharedPreferences.edit().putBoolean("biometric_enabled", value).apply()

    // Pending sync status for resilient registration
    enum class SyncStatus { NONE, PENDING, COMPLETE, FAILED }
    
    var pendingSyncStatus: SyncStatus
        get() = try {
            SyncStatus.valueOf(
                sharedPreferences.getString("pending_sync_status", SyncStatus.NONE.name) ?: SyncStatus.NONE.name
            )
        } catch (e: Exception) {
            SyncStatus.NONE
        }
        private set(value) = sharedPreferences.edit()
            .putString("pending_sync_status", value.name)
            .apply()

    var pendingSyncEmail: String?
        get() = sharedPreferences.getString("pending_sync_email", null)
        set(value) = sharedPreferences.edit().putString("pending_sync_email", value).apply()

    var pendingSyncPassword: String?
        get() = sharedPreferences.getString("pending_sync_password", null)
        private set(value) = sharedPreferences.edit().putString("pending_sync_password", value).apply()

    var pendingSyncDisplayName: String?
        get() = sharedPreferences.getString("pending_sync_display_name", null)
        private set(value) = sharedPreferences.edit().putString("pending_sync_display_name", value).apply()

    var pendingSyncUsername: String?
        get() = sharedPreferences.getString("pending_sync_username", null)
        private set(value) = sharedPreferences.edit().putString("pending_sync_username", value).apply()

    fun setPendingSync(email: String, password: String, displayName: String, username: String) {
        sharedPreferences.edit()
            .putString("pending_sync_status", SyncStatus.PENDING.name)
            .putString("pending_sync_email", email)
            .putString("pending_sync_password", password)
            .putString("pending_sync_display_name", displayName)
            .putString("pending_sync_username", username)
            .apply()
    }

    fun setPendingSyncComplete() {
        sharedPreferences.edit()
            .putString("pending_sync_status", SyncStatus.COMPLETE.name)
            .remove("pending_sync_password") // Clear sensitive data
            .apply()
    }

    fun setPendingSyncFailed() {
        sharedPreferences.edit()
            .putString("pending_sync_status", SyncStatus.FAILED.name)
            .apply()
    }

    fun clearPendingSync() {
        sharedPreferences.edit()
            .remove("pending_sync_status")
            .remove("pending_sync_email")
            .remove("pending_sync_password")
            .remove("pending_sync_display_name")
            .remove("pending_sync_username")
            .apply()
    }

    fun hasPendingSync(): Boolean = pendingSyncStatus == SyncStatus.PENDING

    fun hasFailedSync(): Boolean = pendingSyncStatus == SyncStatus.FAILED
    
    fun clearAuth() {
        // Preserve app-level settings that shouldn't be reset on logout
        val onboarded = isOnboarded
        val theme = themeMode
        
        sharedPreferences.edit().clear().apply()
        
        // Restore preserved settings
        isOnboarded = onboarded
        themeMode = theme
    }

    // Settings
    var themeMode: String
        get() = sharedPreferences.getString("theme_mode", "system") ?: "system"
        set(value) = sharedPreferences.edit().putString("theme_mode", value).apply()

    var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean("notifications_enabled", true)
        set(value) = sharedPreferences.edit().putBoolean("notifications_enabled", value).apply()

    var isPrivateAccount: Boolean
        get() = sharedPreferences.getBoolean("is_private_account", false)
        set(value) = sharedPreferences.edit().putBoolean("is_private_account", value).apply()

    var fcmToken: String?
        get() = sharedPreferences.getString("fcm_token", null)
        set(value) = sharedPreferences.edit().putString("fcm_token", value).apply()
}
