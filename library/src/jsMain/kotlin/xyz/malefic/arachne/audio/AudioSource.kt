package xyz.malefic.arachne.audio

import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.Vector2

data class AudioSource(
    var audioPath: String,
    var volume: Float = 1f,
    var pitch: Float = 1f,
    var loop: Boolean = false,
    var playOnAwake: Boolean = false,
    var is3D: Boolean = false,
    var minDistance: Float = 1f,
    var maxDistance: Float = 100f,
    var isPlaying: Boolean = false,
    private var audioId: String? = null,
) : Component {
    fun play() {
        if (isPlaying) stop()
        audioId = AudioManager.playSound(audioPath, volume, pitch, loop)
        isPlaying = audioId != null
    }

    fun stop() {
        audioId?.let { AudioManager.stopSound(it) }
        audioId = null
        isPlaying = false
    }

    fun pause() {
        // TODO: Implement pause functionality
    }

    fun resume() {
        // TODO: Implement resume functionality
    }

    fun updateVolume(
        listenerPosition: Vector2?,
        sourcePosition: Vector2,
    ) {
        if (!is3D || listenerPosition == null) return

        val distance = listenerPosition.distance(sourcePosition)
        val attenuatedVolume =
            when {
                distance <= minDistance -> {
                    volume
                }

                distance >= maxDistance -> {
                    0f
                }

                else -> {
                    val t = (distance - minDistance) / (maxDistance - minDistance)
                    volume * (1f - t)
                }
            }

        audioId?.let { AudioManager.setMusicVolume(it, attenuatedVolume) }
    }
}
