package com.ninety5.habitate.core.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("[${tag ?: "App"}] priority=$priority message=$message")

        if (t != null) {
            crashlytics.recordException(t)
        }
    }
}
