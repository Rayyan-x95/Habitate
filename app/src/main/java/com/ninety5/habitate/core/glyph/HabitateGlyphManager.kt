package com.ninety5.habitate.core.glyph

import android.content.ComponentName
import android.content.Context
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Habitate Glyph Manager - Controls Nothing Phone's Glyph lights.
 * 
 * Features:
 * - Habit completion feedback (success animation)
 * - Streak milestone celebrations
 * - Notification indicators
 * - Progress visualization
 * - Pomodoro timer indicators
 * 
 * Usage:
 * 1. Call init() in Application.onCreate()
 * 2. Call release() in Application.onTerminate()
 * 3. Use playSuccess(), playStreak(), etc. for feedback
 */
@Singleton
class HabitateGlyphManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var glyphManager: GlyphManager? = null
    private var isSessionOpen = false
    private var isGlyphSupported = false
    
    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()
    
    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName?) {
            Timber.d("Glyph service connected")
            try {
                // Register device-specific Glyph interface
                when {
                    Common.is20111() -> glyphManager?.register(com.nothing.ketchum.Glyph.DEVICE_20111)
                    Common.is22111() -> glyphManager?.register(com.nothing.ketchum.Glyph.DEVICE_22111)
                    Common.is23111() -> glyphManager?.register(com.nothing.ketchum.Glyph.DEVICE_23111)
                    Common.is23113() -> glyphManager?.register(com.nothing.ketchum.Glyph.DEVICE_23113)
                    Common.is24111() -> glyphManager?.register(com.nothing.ketchum.Glyph.DEVICE_24111)
                    else -> {
                        Timber.w("Device not supported for Glyph")
                        isGlyphSupported = false
                        return
                    }
                }
                
                isGlyphSupported = true
                glyphManager?.openSession()
                isSessionOpen = true
                _isAvailable.value = true
                
                Timber.i("Glyph initialized successfully")
            } catch (e: GlyphException) {
                Timber.e(e, "Failed to initialize Glyph")
                isGlyphSupported = false
            }
        }
        
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Timber.d("Glyph service disconnected")
            isSessionOpen = false
            _isAvailable.value = false
        }
    }
    
    /**
     * Initialize Glyph manager.
     * Call this in Application.onCreate()
     */
    fun init() {
        try {
            glyphManager = GlyphManager.getInstance(context)
            glyphManager?.init(callback)
            Timber.d("Glyph manager init called")
        } catch (e: Exception) {
            Timber.w(e, "Glyph not available on this device")
            isGlyphSupported = false
        }
    }
    
    /**
     * Release Glyph resources.
     * Call this in Application.onTerminate() or when no longer needed.
     */
    fun release() {
        try {
            if (isSessionOpen) {
                glyphManager?.closeSession()
                isSessionOpen = false
            }
            glyphManager?.unInit()
            _isAvailable.value = false
            Timber.d("Glyph manager released")
        } catch (e: Exception) {
            Timber.e(e, "Error releasing Glyph manager")
        }
    }
    
    /**
     * Play success animation for habit completion.
     * Quick pulse with all glyphs.
     */
    fun playHabitSuccess() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            // All glyphs quick pulse
            val frame = builder
                .buildChannelA()
                .buildChannelB()
                .buildChannelC()
                .buildChannelD()
                .buildChannelE()
                .buildPeriod(300)  // 300ms pulse
                .buildCycles(2)     // 2 pulses
                .buildInterval(100) // 100ms between pulses
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing habit success animation")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing success animation")
        }
    }
    
    /**
     * Play streak milestone celebration.
     * More dramatic animation for milestone achievements.
     */
    fun playStreakMilestone(streak: Int) {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            // Dramatic celebration: 3 cycles, longer period
            val frame = builder
                .buildChannelA()
                .buildChannelB()
                .buildChannelC()
                .buildChannelD()
                .buildChannelE()
                .buildPeriod(500)   // 500ms pulse
                .buildCycles(3)      // 3 pulses
                .buildInterval(150)  // 150ms between pulses
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing streak milestone animation for $streak days")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing streak animation")
        }
    }
    
    /**
     * Display progress on C1 zone (camera ring).
     * Perfect for showing habit completion progress.
     * 
     * @param progress Progress value 0-100
     */
    fun displayProgress(progress: Int) {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            val frame = builder.buildChannelC().build()
            
            glyphManager?.displayProgress(frame, progress.coerceIn(0, 100))
            Timber.d("Displaying progress: $progress%")
        } catch (e: GlyphException) {
            Timber.e(e, "Error displaying progress")
        }
    }
    
    /**
     * Toggle glyphs for notification indicator.
     * Subtle single pulse to indicate new notification.
     */
    fun playNotificationIndicator() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            // Single subtle pulse on camera ring
            val frame = builder
                .buildChannelC()
                .buildPeriod(200)
                .buildCycles(1)
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing notification indicator")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing notification")
        }
    }
    
    /**
     * Pomodoro timer indicator.
     * Pulsing animation to show focus mode active.
     */
    fun playPomodoroActive() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            // Slow breathing animation
            val frame = builder
                .buildChannelE()  // Top strip
                .buildPeriod(2000)  // 2s slow pulse
                .buildCycles(1)
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing Pomodoro active indicator")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing Pomodoro indicator")
        }
    }
    
    /**
     * Pomodoro completion celebration.
     */
    fun playPomodoroComplete() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            val frame = builder
                .buildChannelA()
                .buildChannelB()
                .buildChannelC()
                .buildPeriod(400)
                .buildCycles(3)
                .buildInterval(100)
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing Pomodoro complete animation")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing Pomodoro complete")
        }
    }
    
    /**
     * Play habit reminder animation.
     * Double pulse on bottom glyphs.
     */
    fun playHabitReminder() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            val builder = glyphManager?.glyphFrameBuilder ?: return
            
            val frame = builder
                .buildChannelA()
                .buildChannelB()
                .buildPeriod(300)
                .buildCycles(2)
                .buildInterval(200)
                .build()
            
            glyphManager?.animate(frame)
            Timber.d("Playing habit reminder animation")
        } catch (e: GlyphException) {
            Timber.e(e, "Error playing habit reminder")
        }
    }

    /**
     * Turn off all glyphs.
     */
    fun turnOff() {
        if (!isGlyphSupported || !isSessionOpen) return
        
        try {
            glyphManager?.turnOff()
            Timber.d("All glyphs turned off")
        } catch (e: Exception) {
            Timber.e(e, "Error turning off glyphs")
        }
    }
    
    /**
     * Custom animation builder for advanced use cases.
     */
    fun getGlyphFrameBuilder(): GlyphFrame.Builder? {
        return if (isGlyphSupported && isSessionOpen) {
            glyphManager?.glyphFrameBuilder
        } else null
    }
}
