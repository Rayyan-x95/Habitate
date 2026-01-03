package com.ninety5.habitate.core.utils

import android.content.ComponentName
import android.content.Context
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import timber.log.Timber

class GlyphInterfaceManager(private val context: Context) {

    private var mGM: GlyphManager? = null
    private var mCallback: GlyphManager.Callback? = null

    fun init() {
        mGM = GlyphManager.getInstance(context)
        mCallback = object : GlyphManager.Callback {
            override fun onServiceConnected(componentName: ComponentName) {
                if (Common.is20111()) mGM?.register(Glyph.DEVICE_20111)
                if (Common.is22111()) mGM?.register(Glyph.DEVICE_22111)
                if (Common.is23111()) mGM?.register(Glyph.DEVICE_23111)
                try {
                    mGM?.openSession()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                try {
                    mGM?.closeSession()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mGM?.init(mCallback)
    }

    fun triggerLight() {
        if (mGM == null) return
        try {
            val builder = mGM?.glyphFrameBuilder
            val frame = builder?.buildChannelA()?.build()
            if (frame != null) {
                mGM?.toggle(frame)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            mGM?.closeSession()
            mGM?.unInit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
