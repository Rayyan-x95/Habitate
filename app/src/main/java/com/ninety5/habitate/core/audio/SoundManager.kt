package com.ninety5.habitate.core.audio

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified audio manager for the Habitate application.
 *
 * Handles both:
 * - Asset-based sounds (e.g. ambient sounds from assets/sounds/)
 * - Resource-based sounds (e.g. raw resource IDs)
 *
 * Provides observable state for UI binding (isPlaying, currentTrack).
 */
@Singleton
class HabitateAudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack.asStateFlow()

    /**
     * Play a sound from an Android raw resource ID.
     */
    fun playResource(resId: Int, trackName: String, looping: Boolean = true) {
        stop()
        try {
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = looping
                start()
            }
            if (mediaPlayer != null) {
                _isPlaying.value = true
                _currentTrack.value = trackName
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to play resource: $trackName")
        }
    }

    /**
     * Play a sound from the assets/sounds/ directory.
     */
    fun playAsset(assetName: String, looping: Boolean = true) {
        stop()
        try {
            val afd = context.assets.openFd("sounds/$assetName")
            afd.use { fd ->
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    isLooping = looping
                    prepare()
                    start()
                }
            }
            _isPlaying.value = true
            _currentTrack.value = assetName
        } catch (e: Exception) {
            Timber.e(e, "Failed to play asset: $assetName")
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            Timber.e(e, "Failed to pause audio")
        }
    }

    fun resume() {
        try {
            val player = mediaPlayer ?: return
            player.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            Timber.e(e, "Failed to resume audio")
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop audio")
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentTrack.value = null
    }
}
