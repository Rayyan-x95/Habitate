package com.ninety5.habitate.util.audio

import com.ninety5.habitate.core.audio.HabitateAudioManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @deprecated Use [HabitateAudioManager] directly instead.
 * This class is kept for backward compatibility and delegates to [HabitateAudioManager].
 */
@Deprecated(
    message = "Use HabitateAudioManager from core/audio/ instead",
    replaceWith = ReplaceWith(
        "HabitateAudioManager",
        "com.ninety5.habitate.core.audio.HabitateAudioManager"
    )
)
@Singleton
class AmbientSoundPlayer @Inject constructor(
    private val audioManager: HabitateAudioManager
) {
    val isPlaying: StateFlow<Boolean> = audioManager.isPlaying
    val currentTrack: StateFlow<String?> = audioManager.currentTrack

    fun play(trackResId: Int, trackName: String) = audioManager.playResource(trackResId, trackName)
    fun stop() = audioManager.stop()
    fun pause() = audioManager.pause()
    fun resume() = audioManager.resume()
}
