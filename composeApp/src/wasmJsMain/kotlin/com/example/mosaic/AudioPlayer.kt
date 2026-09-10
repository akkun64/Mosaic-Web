package com.example.mosaic

import org.w3c.files.File

external object AudioBridge {
    fun load(url: String)
    fun play()
    fun pause()
    fun seek(ms: Double)
    fun getTime(): Double
    fun getDur(): Double
}

object AudioPlayer {
    var onEnded: () -> Unit = {}

    fun init() {
        val bridge = js("window._mosaicAudio")
        AudioBridge.load = bridge.load.unsafeCast<kotlin.js.Function1<String, Unit>>()
        AudioBridge.play = bridge.play.unsafeCast<kotlin.js.Function0<Unit>>()
        AudioBridge.pause = bridge.pause.unsafeCast<kotlin.js.Function0<Unit>>()
        AudioBridge.seek = bridge.seek.unsafeCast<kotlin.js.Function1<Double, Unit>>()
        AudioBridge.getTime = bridge.getTime.unsafeCast<kotlin.js.Function0<Double>>()
        AudioBridge.getDur = bridge.getDur.unsafeCast<kotlin.js.Function0<Double>>()
        bridge.onEnd = { onEnded() }
    }

    fun load(file: File) {
        val url = js("URL.createObjectURL")(file) as String
        AudioBridge.load(url)
    }

    fun play() = AudioBridge.play()
    fun pause() = AudioBridge.pause()
    fun seek(ms: Double) = AudioBridge.seek(ms)
    fun getCurrentTime() = AudioBridge.getTime()
    fun getDuration() = AudioBridge.getDur()
}
