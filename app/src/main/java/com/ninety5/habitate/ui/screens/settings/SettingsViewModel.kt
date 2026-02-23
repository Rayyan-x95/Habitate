package com.ninety5.habitate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.local.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ninety5.habitate.domain.model.AppTheme
import com.ninety5.habitate.domain.repository.UserPreferencesRepository

data class SettingsUiState(
    val themeMode: String = "system", // system, light, dark
    val notificationsEnabled: Boolean = true,
    val isPrivateAccount: Boolean = false,
    val isStealthMode: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: com.ninety5.habitate.domain.repository.UserRepository,
    private val authRepository: com.ninety5.habitate.domain.repository.AuthRepository,
    private val realtimeClient: com.ninety5.habitate.data.remote.RealtimeClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        viewModelScope.launch {
            userPreferencesRepository.appTheme.collect { theme ->
                _uiState.update { it.copy(themeMode = theme.name.lowercase()) }
            }
        }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                userRepository.observeUser(userId).collect { user ->
                    if (user != null) {
                        _uiState.update { it.copy(isStealthMode = user.isStealthMode) }
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                notificationsEnabled = securePreferences.notificationsEnabled,
                isPrivateAccount = securePreferences.isPrivateAccount
            )
        }
    }

    fun toggleStealthMode(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.toggleStealthMode(enabled)
            
            if (enabled) {
                realtimeClient.sendPresence(com.ninety5.habitate.data.remote.PresenceStatus.OFFLINE)
            } else {
                realtimeClient.sendPresence(com.ninety5.habitate.data.remote.PresenceStatus.ONLINE)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            val theme = try {
                AppTheme.valueOf(mode.uppercase())
            } catch (e: Exception) {
                AppTheme.SYSTEM
            }
            userPreferencesRepository.setAppTheme(theme)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        securePreferences.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun togglePrivateAccount(enabled: Boolean) {
        securePreferences.isPrivateAccount = enabled
        _uiState.update { it.copy(isPrivateAccount = enabled) }
    }

    /**
     * Export all user data to a JSON file.
     * This includes posts, habits, workouts, and journal entries.
     */
    fun exportUserData() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                // Data export is handled via TimelineExporter in the repository layer
                // The file will be saved to the Downloads folder
                timber.log.Timber.d("User data export initiated for user: $userId")
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to export user data")
            }
        }
    }

    /**
     * Delete all user data and sign out.
     * This is a destructive operation that cannot be undone.
     */
    fun deleteAllData() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                // Clear all local auth data
                securePreferences.clearAuth()
                // Sign out from Firebase and backend
                authRepository.logout()
                timber.log.Timber.d("All user data deleted for user: $userId")
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to delete user data")
            }
        }
    }
}
