package com.ninety5.habitate.data.remote

import com.ninety5.habitate.data.local.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding auth header if already present (e.g. refresh token request)
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val builder = originalRequest.newBuilder()
        
        securePreferences.accessToken?.let { token ->
            builder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
