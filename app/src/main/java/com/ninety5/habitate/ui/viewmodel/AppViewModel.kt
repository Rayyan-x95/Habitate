package com.ninety5.habitate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.core.auth.SessionManager
import com.ninety5.habitate.core.auth.SessionState
import com.ninety5.habitate.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * App-level ViewModel for cross-cutting concerns.
 *
 * Handles:
 * - Observing [SessionManager] for auth state changes
 * - Initializing realtime chat when user becomes authenticated
 * - Exposing [SessionState] for navigation decisions
 *
 * This ViewModel is scoped to the Activity and avoids injecting
 * repositories directly into the Activity class.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    /** Expose session state for nav decisions in the Activity/Composable layer. */
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, sessionManager.sessionState.value)

    init {
        observeSessionForServices()
    }

    /**
     * Observe session state and initialize services when authenticated.
     */
    private fun observeSessionForServices() {
        viewModelScope.launch {
            sessionManager.sessionState.collectLatest { state ->
                when (state) {
                    is SessionState.Authenticated -> {
                        try {
                            chatRepository.initializeRealtime()
                            Timber.d("AppViewModel: Realtime chat initialized for ${state.userId}")
                        } catch (e: Exception) {
                            Timber.e(e, "AppViewModel: Failed to initialize realtime chat")
                        }
                    }
                    is SessionState.SessionExpired -> {
                        try {
                            chatRepository.disconnectRealtime()
                        } catch (e: Exception) {
                            Timber.e(e, "AppViewModel: Failed to disconnect realtime chat")
                        }
                        Timber.w("AppViewModel: Session expired — navigation should redirect to login")
                    }
                    is SessionState.Unauthenticated -> {
                        try {
                            chatRepository.disconnectRealtime()
                        } catch (e: Exception) {
                            Timber.e(e, "AppViewModel: Failed to disconnect realtime chat")
                        }
                        Timber.d("AppViewModel: User is unauthenticated")
                    }
                }
            }
        }
    }

    /**
     * Clear session expired state after user has been redirected to login.
     */
    fun onSessionExpiredHandled() {
        sessionManager.clearExpiredState()
    }
}
