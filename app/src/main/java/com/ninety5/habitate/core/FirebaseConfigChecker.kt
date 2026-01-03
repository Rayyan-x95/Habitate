package com.ninety5.habitate.core

import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase configuration checker.
 * Detects whether various Firebase features are properly configured
 * before exposing them to users.
 */
@Singleton
class FirebaseConfigChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Domain patterns that require Dynamic Links
        private val DYNAMIC_LINK_DOMAINS = listOf(
            ".page.link",
            ".app.goo.gl"
        )
        
        // Valid domains for email link auth that don't require Dynamic Links
        private val ALLOWED_AUTH_DOMAINS = listOf(
            "firebaseapp.com"
        )
    }

    /**
     * Check if Firebase Dynamic Links is properly configured.
     * 
     * Dynamic Links requires:
     * 1. Firebase Dynamic Links SDK dependency
     * 2. Dynamic Links domain configured in Firebase Console
     * 3. Domain verified with app
     * 
     * For email link authentication:
     * - If using .page.link or custom domain: Dynamic Links MUST be configured
     * - If using firebaseapp.com hosting domain: No Dynamic Links needed but limited
     */
    fun isDynamicLinksConfigured(): Boolean {
        return try {
            // Check if Dynamic Links class is available (SDK included)
            Class.forName("com.google.firebase.dynamiclinks.FirebaseDynamicLinks")
            
            // Check if Firebase is initialized
            if (FirebaseApp.getApps(context).isEmpty()) {
                Timber.w("FirebaseConfigChecker: Firebase not initialized")
                return false
            }
            
            // Check for Dynamic Links domain in manifest
            val hasDynamicLinksDomain = checkManifestForDynamicLinks()
            
            if (!hasDynamicLinksDomain) {
                Timber.w("FirebaseConfigChecker: No Dynamic Links domain configured")
            }
            
            hasDynamicLinksDomain
        } catch (e: ClassNotFoundException) {
            Timber.d("FirebaseConfigChecker: Dynamic Links SDK not included")
            false
        } catch (e: Exception) {
            Timber.e(e, "FirebaseConfigChecker: Error checking Dynamic Links")
            false
        }
    }

    /**
     * Check if email link authentication can be safely offered.
     * 
     * Requirements:
     * - Firebase Auth configured
     * - ActionCodeSettings URL uses a valid domain
     * - If URL uses Dynamic Links domain, Dynamic Links must be configured
     */
    fun isEmailLinkAuthAvailable(): Boolean {
        // Check basic Firebase setup
        if (FirebaseApp.getApps(context).isEmpty()) {
            Timber.w("FirebaseConfigChecker: Firebase not initialized, email link auth unavailable")
            return false
        }

        // For now, we check if Dynamic Links is configured
        // If not configured, email link auth will fail at runtime
        val dynamicLinksReady = isDynamicLinksConfigured()
        
        // Alternative: Check if we have a simple hosting URL that doesn't need Dynamic Links
        // For production, you would configure this properly
        
        if (!dynamicLinksReady) {
            Timber.w("FirebaseConfigChecker: Email link auth unavailable - Dynamic Links not configured")
        }
        
        return dynamicLinksReady
    }

    /**
     * Get user-friendly message for why email link auth is unavailable.
     */
    fun getEmailLinkUnavailableReason(): String {
        return when {
            FirebaseApp.getApps(context).isEmpty() -> 
                "Sign in with email link is temporarily unavailable"
            !isDynamicLinksConfigured() -> 
                "Sign in with email link is currently unavailable"
            else -> 
                "Sign in with email link is temporarily unavailable"
        }
    }

    private fun checkManifestForDynamicLinks(): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            
            val metaData = appInfo.metaData
            if (metaData != null) {
                // Check for common Dynamic Links metadata keys
                val hasLinksDomain = metaData.containsKey("com.google.firebase.dynamiclinks.default_domain")
                if (hasLinksDomain) return true
            }
            
            // Check for intent filters that indicate Dynamic Links setup
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                addCategory(android.content.Intent.CATEGORY_DEFAULT)
                addCategory(android.content.Intent.CATEGORY_BROWSABLE)
                data = android.net.Uri.parse("https://example.page.link/test")
            }
            
            val resolvedActivities = context.packageManager.queryIntentActivities(intent, 0)
            resolvedActivities.any { it.activityInfo.packageName == context.packageName }
        } catch (e: Exception) {
            Timber.e(e, "Error checking manifest for Dynamic Links")
            false
        }
    }
}
