package com.ninety5.habitate.core.utils

import android.content.Context
import timber.log.Timber

class GlyphInterfaceManager(private val context: Context) {

    // TODO: Fix Nothing Glyph SDK integration
    // Currently disabled for public beta readiness
    
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
