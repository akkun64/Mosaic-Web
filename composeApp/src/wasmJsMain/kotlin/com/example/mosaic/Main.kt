package com.example.mosaic

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AudioPlayer.init()

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
