package com.ninety5.habitate.data.remote

import com.ninety5.habitate.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    companion object {
        private const val REFRESH_TIMEOUT_MS = 10_000L
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // If the request already has the "No-Auth" header, give up
        if (response.request.header("No-Auth") == "true") {
            return null
        }

        // Prevent infinite loops: if we've tried 3 times, give up
        if (responseCount(response) >= 3) {
            Timber.w("TokenAuthenticator: Max retries reached, giving up")
            return null
        }

        val authRepository = authRepositoryProvider.get()

        // Synchronously refresh the token with timeout to prevent ANR
        val newToken = try {
            runBlocking {
                withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                    authRepository.refreshAccessToken().getOrNull()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "TokenAuthenticator: Token refresh failed")
            null
        } ?: return null

        // Retry the request with the new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
