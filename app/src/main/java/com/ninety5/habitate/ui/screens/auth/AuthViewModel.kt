package com.ninety5.habitate.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.result.AppResult
import com.ninety5.habitate.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Track registration status for better UX
    val registrationStatus: RegistrationStatus = RegistrationStatus.Idle,
    // Email link auth availability
    val isEmailLinkAvailable: Boolean = false,
    val emailLinkUnavailableReason: String? = null,
    // Show success message for email link
    val emailLinkSent: Boolean = false,
    // Password reset
    val passwordResetSent: Boolean = false,
    // Email verification
    val isEmailVerified: Boolean = false,
    val verificationEmailSent: Boolean = false
)

/**
 * Registration status for nuanced UX messaging.
 * Firebase success is FINAL - backend sync is just setup completion.
 */
sealed class RegistrationStatus {
    object Idle : RegistrationStatus()
    object InProgress : RegistrationStatus()
    
    /**
     * Account created successfully.
     * @param backendSynced Whether backend sync completed immediately
     */
    data class Success(val backendSynced: Boolean) : RegistrationStatus()
    
    data class Failed(val message: String) : RegistrationStatus()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuthenticated ->
                _uiState.update { it.copy(isLoggedIn = isAuthenticated) }
            }
        }
        _uiState.update { 
            it.copy(
                isOnboarded = authRepository.isOnboarded(),
                isEmailLinkAvailable = authRepository.isEmailLinkAuthAvailable(),
                emailLinkUnavailableReason = if (!authRepository.isEmailLinkAuthAvailable()) {
                    authRepository.getEmailLinkUnavailableReason()
                } else null
            )
        }
        
        // Retry any pending backend sync on init
        authRepository.retryPendingSyncIfNeeded()
    }

    fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun login(email: String, password: String) {
        // Input validation
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your password") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, password)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun register(email: String, password: String, displayName: String, username: String) {
        // Input validation
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a password") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }
        if (displayName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your display name") }
            return
        }
        if (username.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a username") }
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))) {
            _uiState.update { it.copy(error = "Username must be 3-20 characters (letters, numbers, underscores only)") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null, 
                    registrationStatus = RegistrationStatus.InProgress
                ) 
            }
            
            authRepository.register(email, password, username, displayName)
                .let { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val hasPendingSync = authRepository.hasPendingBackendSync()
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    registrationStatus = RegistrationStatus.Success(
                                        backendSynced = !hasPendingSync
                                    )
                                ) 
                            }
                        }
                        is AppResult.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = result.error.message,
                                    registrationStatus = RegistrationStatus.Failed(
                                        result.error.message
                                    )
                                ) 
                            }
                        }
                        is AppResult.Loading -> { /* no-op */ }
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearEmailLinkSent() {
        _uiState.update { it.copy(emailLinkSent = false) }
    }
    
    fun sendSignInLink(email: String) {
        // Pre-check availability
        if (!authRepository.isEmailLinkAuthAvailable()) {
            _uiState.update { 
                it.copy(error = authRepository.getEmailLinkUnavailableReason()) 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, emailLinkSent = false) }
            authRepository.sendSignInLinkToEmail(email)
                .let { result ->
                    when (result) {
                        is AppResult.Success -> _uiState.update { 
                            it.copy(isLoading = false, emailLinkSent = true) 
                        }
                        is AppResult.Error -> _uiState.update { 
                            it.copy(isLoading = false, error = result.error.message) 
                        }
                        is AppResult.Loading -> { /* no-op */ }
                    }
                }
        }
    }
    
    /**
     * Send password reset email.
     */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, passwordResetSent = false) }
            authRepository.sendPasswordResetEmail(email)
                .let { result ->
                    when (result) {
                        is AppResult.Success -> _uiState.update {
                            it.copy(isLoading = false, passwordResetSent = true)
                        }
                        is AppResult.Error -> _uiState.update { 
                            it.copy(isLoading = false, error = result.error.message) 
                        }
                        is AppResult.Loading -> { /* no-op */ }
                    }
                }
        }
    }

    fun clearPasswordResetSent() {
        _uiState.update { it.copy(passwordResetSent = false) }
    }

    fun handleDeepLink(link: String) {
        if (authRepository.isSignInWithEmailLink(link)) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                authRepository.signInWithEmailLink("", link)
                    .let { result ->
                        when (result) {
                            is AppResult.Success -> _uiState.update { it.copy(isLoading = false) }
                            is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                            is AppResult.Loading -> { /* no-op */ }
                        }
                    }
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.reloadUser()) {
                is AppResult.Success -> {
                    val isVerified = authRepository.isEmailVerified()
                    _uiState.update { it.copy(isLoading = false, isEmailVerified = isVerified) }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, verificationEmailSent = false) }
            when (val result = authRepository.sendEmailVerification()) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, verificationEmailSent = true) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedIn = false, isEmailVerified = false) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.deleteAccount()) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                is AppResult.Loading -> { /* no-op */ }
            }
        }
    }
}
