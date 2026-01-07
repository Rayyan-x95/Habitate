package com.ninety5.habitate.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║                      SPLASH SCREEN VIEWMODEL                             ║
 * ║                                                                          ║
 * ║  Handles splash screen logic:                                           ║
 * ║  • Check authentication status                                          ║
 * ║  • Determine navigation destination                                     ║
 * ║  • Manage splash timing                                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Simulate minimum splash duration
                delay(1000)
                
                val authState = authRepository.getAuthState()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = authState.isLoggedIn,
                    isOnboarded = authState.isOnboarded,
                    isEmailVerified = authState.isEmailVerified
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

/**
 * Splash screen UI state
 */
data class SplashUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false,
    val isEmailVerified: Boolean = false,
    val error: String? = null
)
