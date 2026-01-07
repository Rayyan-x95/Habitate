package com.ninety5.habitate.core.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber

class GlyphMatrixController(private val context: Context) {

    // TODO: Fix Nothing Glyph SDK integration
    // Currently disabled for public beta readiness
    
    fun init() {
        Timber.d("GlyphMatrixController initialized (disabled)")
    }

    fun displayImage(bitmap: Bitmap) {
        Timber.d("GlyphMatrixController displayImage called (disabled)")
    }
}
