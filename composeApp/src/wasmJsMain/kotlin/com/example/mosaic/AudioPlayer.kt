package com.example.mosaic

object AudioPlayer {
    fun init() {
        js("window._mosaicInit()")
    }

    fun loadByIndex(index: Int) {
        js("window._mosaicLoadByIndex(index)")
    }

    fun play() {
        js("window._mosaicPlay()")
    }

    fun pause() {
        js("window._mosaicPause()")
    }

    fun seek(ms: Double) {
        js("window._mosaicSeek(ms)")
    }

    fun getCurrentTime(): Double {
        return js("window._mosaicGetTime()")
    }

    fun getDuration(): Double {
        return js("window._mosaicGetDur()")
    }

    fun setOnEnded(block: () -> Unit) {
        js("window._mosaicSetOnEnd(block)")
    }

    fun openFilePicker(callback: () -> Unit) {
        js("window._mosaicOpenPicker(callback)")
    }

    fun getFileCount(): Int {
        return js("window._mosaicFiles.length")
    }

    fun getFileName(index: Int): String {
        return js("window._mosaicFiles[index].name")
    }
}
