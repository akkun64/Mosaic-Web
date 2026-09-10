package com.example.mosaic

import org.w3c.files.File

private fun jsEval(code: String): dynamic = js("(function() { return $code })()")

object AudioPlayer {
    var onEnded: () -> Unit = {}

    private val audio: dynamic = js("new Audio()")
    private var onEndFn: (() -> Unit)? = null

    fun init() {
        audio.onended = { onEndFn?.invoke() }
    }

    fun load(file: File) {
        val url: dynamic = js("URL.createObjectURL")(file)
        audio.src = url
        audio.load()
    }

    fun play() { audio.play() }
    fun pause() { audio.pause() }
    fun seek(ms: Double) { audio.currentTime = ms / 1000.0 }
    fun getCurrentTime(): Double = (audio.currentTime as? Double ?: 0.0) * 1000.0
    fun getDuration(): Double = (audio.duration as? Double ?: 0.0) * 1000.0

    fun setOnEnded(block: () -> Unit) {
        onEndFn = block
    }
}
