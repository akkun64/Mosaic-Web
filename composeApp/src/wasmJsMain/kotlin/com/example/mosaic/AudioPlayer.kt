package com.example.mosaic

private fun initJs(): Unit = js("window._mosaicInit()")
private fun loadByIndexJs(index: Int): Unit = js("window._mosaicLoadByIndex(index)")
private fun playJs(): Unit = js("window._mosaicPlay()")
private fun pauseJs(): Unit = js("window._mosaicPause()")
private fun seekJs(ms: Double): Unit = js("window._mosaicSeek(ms)")
private fun getTimeJs(): Double = js("window._mosaicGetTime()")
private fun getDurJs(): Double = js("window._mosaicGetDur()")
private fun setOnEndJs(block: () -> Unit): Unit = js("window._mosaicSetOnEnd(block)")
private fun openPickerJs(callback: () -> Unit): Unit = js("window._mosaicOpenPicker(callback)")
private fun getFileCountJs(): Int = js("window._mosaicFiles.length")
private fun getFileNameJs(index: Int): String = js("window._mosaicFiles[index].name")

object AudioPlayer {
    fun init() { initJs() }
    fun loadByIndex(index: Int) { loadByIndexJs(index) }
    fun play() { playJs() }
    fun pause() { pauseJs() }
    fun seek(ms: Double) { seekJs(ms) }
    fun getCurrentTime(): Double = getTimeJs()
    fun getDuration(): Double = getDurJs()
    fun setOnEnded(block: () -> Unit) { setOnEndJs(block) }
    fun openFilePicker(callback: () -> Unit) { openPickerJs(callback) }
    fun getFileCount(): Int = getFileCountJs()
    fun getFileName(index: Int): String = getFileNameJs(index)
}
