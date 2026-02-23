package com.ninety5.habitate.core.utils

import android.content.Context
import timber.log.Timber

class GlyphInterfaceManager(private val context: Context) {

    // Nothing Glyph SDK integration is disabled for public beta.
    // The Nothing Glyph SDK dependency must be added to build.gradle.kts and the
    // GlyphManager API initialised here once the SDK is stable and available.
    // See: https://github.com/nickmalleson/nothing-glyph-developer-kit
    
    fun init() {
        Timber.d("GlyphInterfaceManager initialized (disabled)")
    }

    fun triggerLight() {
        Timber.d("GlyphInterfaceManager triggerLight called (disabled)")
    }

    fun close() {
        Timber.d("GlyphInterfaceManager close called (disabled)")
    }
}
