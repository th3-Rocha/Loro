package com.rocha.louro.audio

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Plays a level's recorded audio when it ships one, otherwise falls back to the device's offline TTS. */
class Speaker(context: Context) {
    private val appContext = context.applicationContext
    private var ready = false
    private var player: MediaPlayer? = null
    private val tts = TextToSpeech(appContext) { status -> ready = status == TextToSpeech.SUCCESS }

    fun speak(text: String, locale: Locale, audioAsset: String? = null) {
        stop()
        if (audioAsset != null && playAsset(audioAsset)) return
        if (!ready) return
        tts.language = locale
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "louro")
    }

    private fun playAsset(path: String): Boolean = runCatching {
        appContext.assets.openFd(path).use { fd ->
            player = MediaPlayer().apply {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                setOnCompletionListener { it.release() }
                prepare()
                start()
            }
        }
    }.isSuccess

    fun stop() {
        runCatching { player?.release() }
        player = null
        if (ready) tts.stop()
    }

    fun release() {
        stop()
        tts.shutdown()
    }
}
