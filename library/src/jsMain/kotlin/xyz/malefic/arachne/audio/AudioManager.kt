package xyz.malefic.arachne.audio

import kotlinx.browser.window
import org.w3c.dom.Audio
import org.w3c.dom.HTMLAudioElement

object AudioManager {
    private val audioCache = mutableMapOf<String, HTMLAudioElement>()
    private val playingAudio = mutableMapOf<String, HTMLAudioElement>()
    private var audioContext: dynamic = null

    var masterVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateAllVolumes()
        }

    var musicVolume: Float = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateAllVolumes()
        }

    var sfxVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateAllVolumes()
        }

    var muted: Boolean = false
        set(value) {
            field = value
            updateAllVolumes()
        }

    init {
        try {
            audioContext = js("new (window.AudioContext || window.webkitAudioContext)()")
        } catch (e: Exception) {
            console.warn("Web Audio API not supported", e)
        }
    }

    fun loadAudio(
        path: String,
        onComplete: ((HTMLAudioElement) -> Unit)? = null,
    ) {
        if (audioCache.containsKey(path)) {
            onComplete?.invoke(audioCache[path]!!)
            return
        }

        val audio = Audio(path)
        audio.onloadeddata = {
            audioCache[path] = audio
            onComplete?.invoke(audio)
            null
        }
        audio.onerror = { _, _, _, _, _ ->
            console.error("Failed to load audio: $path")
            null
        }
    }

    fun preloadAudio(
        paths: List<String>,
        onComplete: (() -> Unit)? = null,
    ) {
        var loaded = 0
        paths.forEach { path ->
            loadAudio(path) {
                loaded++
                if (loaded == paths.size) {
                    onComplete?.invoke()
                }
            }
        }
    }

    fun playSound(
        path: String,
        volume: Float = 1f,
        pitch: Float = 1f,
        loop: Boolean = false,
    ): String? {
        val audio =
            audioCache[path] ?: run {
                loadAudio(path)
                return null
            }

        val instance = audio.cloneNode(true) as HTMLAudioElement
        val finalVolume = if (muted) 0.0 else (masterVolume * sfxVolume * volume).toDouble()

        instance.volume = finalVolume
        instance.playbackRate = pitch.toDouble()
        instance.loop = loop

        val id = "${path}_${window.performance.now().toLong()}"
        playingAudio[id] = instance

        instance.onended = {
            playingAudio.remove(id)
            null
        }

        instance.play()
        return id
    }

    fun playMusic(
        path: String,
        volume: Float = 1f,
        loop: Boolean = true,
        fadeIn: Float = 0f,
    ): String? {
        stopAllMusic()

        val audio =
            audioCache[path] ?: run {
                loadAudio(path)
                return null
            }

        val instance = audio.cloneNode(true) as HTMLAudioElement
        val finalVolume = if (muted) 0.0 else (masterVolume * musicVolume * volume).toDouble()

        instance.volume = if (fadeIn > 0) 0.0 else finalVolume
        instance.loop = loop

        val id = "music_$path"
        playingAudio[id] = instance

        instance.play()

        if (fadeIn > 0) {
            fadeVolume(instance, 0.0, finalVolume, fadeIn)
        }

        return id
    }

    fun stopSound(id: String) {
        playingAudio[id]?.let { audio ->
            audio.pause()
            audio.currentTime = 0.0
            playingAudio.remove(id)
        }
    }

    fun stopAllMusic() {
        playingAudio.keys.filter { it.startsWith("music_") }.forEach { id ->
            stopSound(id)
        }
    }

    fun stopAllSounds() {
        playingAudio.keys.toList().forEach { id ->
            if (!id.startsWith("music_")) {
                stopSound(id)
            }
        }
    }

    fun pauseSound(id: String) {
        playingAudio[id]?.pause()
    }

    fun resumeSound(id: String) {
        playingAudio[id]?.play()
    }

    fun pauseAll() {
        playingAudio.values.forEach { it.pause() }
    }

    fun resumeAll() {
        playingAudio.values.forEach { it.play() }
    }

    fun setMusicVolume(
        id: String,
        volume: Float,
        duration: Float = 0f,
    ) {
        playingAudio[id]?.let { audio ->
            val finalVolume = if (muted) 0.0 else (masterVolume * musicVolume * volume).toDouble()
            if (duration > 0) {
                fadeVolume(audio, audio.volume, finalVolume, duration)
            } else {
                audio.volume = finalVolume
            }
        }
    }

    private fun fadeVolume(
        audio: HTMLAudioElement,
        fromVolume: Double,
        toVolume: Double,
        duration: Float,
    ) {
        val startTime = window.performance.now()
        val volumeDiff = toVolume - fromVolume

        fun updateVolume() {
            val elapsed = (window.performance.now() - startTime) / 1000.0
            val progress = (elapsed / duration).coerceIn(0.0, 1.0)

            audio.volume = fromVolume + (volumeDiff * progress)

            if (progress < 1.0) {
                window.requestAnimationFrame { updateVolume() }
            }
        }

        updateVolume()
    }

    private fun updateAllVolumes() {
        playingAudio.forEach { (id, audio) ->
            val volume =
                if (id.startsWith("music_")) {
                    masterVolume * musicVolume
                } else {
                    masterVolume * sfxVolume
                }
            audio.volume = if (muted) 0.0 else volume.toDouble()
        }
    }

    fun unload(path: String) {
        audioCache.remove(path)
    }

    fun unloadAll() {
        stopAllMusic()
        stopAllSounds()
        audioCache.clear()
        playingAudio.clear()
    }

    fun isPlaying(id: String) = playingAudio.containsKey(id)

    fun hasAudio(path: String) = audioCache.containsKey(path)
}
