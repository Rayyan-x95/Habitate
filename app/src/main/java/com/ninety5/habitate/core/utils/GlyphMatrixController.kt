package com.ninety5.habitate.core.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import timber.log.Timber

class GlyphMatrixController(private val context: Context) {

    private var mGM: GlyphMatrixManager? = null

    fun init() {
        mGM = GlyphMatrixManager.getInstance(context)
        mGM?.init(object : GlyphMatrixManager.Callback {
            override fun onServiceConnected(name: ComponentName) {
                // Register for compatible device (e.g., Phone (2a) Plus)
                // mGM?.register(Glyph.DEVICE_23112) 
                Timber.d("Service connected")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Timber.d("Service disconnected")
            }
        })
    }

    fun displayImage(bitmap: Bitmap) {
        if (mGM == null) return
        try {
            val obj = GlyphMatrixObject.Builder()
                .setImageSource(bitmap)
                .build()

            val frame = GlyphMatrixFrame.Builder()
                .addTop(obj)
                .build(context)

            mGM?.setAppMatrixFrame(frame.render())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
