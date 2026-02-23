package com.ninety5.habitate.core.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber

class GlyphMatrixController(private val context: Context) {

    // Nothing Glyph SDK integration is disabled for public beta.
    // The Nothing Glyph SDK dependency must be added to build.gradle.kts and the
    // GlyphFrame / MatrixComposer APIs initialised here once the SDK is stable.
    // See: https://github.com/nickmalleson/nothing-glyph-developer-kit
    
    fun init() {
        Timber.d("GlyphMatrixController initialized (disabled)")
    }

    fun displayImage(bitmap: Bitmap) {
        Timber.d("GlyphMatrixController displayImage called (disabled)")
    }
}
