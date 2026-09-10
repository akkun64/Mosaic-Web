package com.example.mosaic

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    js(
        """
        var _audio = new Audio();
        var _onEnded = null;
        _audio.addEventListener('ended', function() { if (_onEnded) _onEnded(); });
        window._mosaicAudio = {
            load: function(url) { _audio.src = url; _audio.load(); },
            play: function() { _audio.play(); },
            pause: function() { _audio.pause(); },
            seek: function(ms) { _audio.currentTime = ms / 1000; },
            getTime: function() { return (_audio.currentTime || 0) * 1000; },
            getDur: function() { return (_audio.duration || 0) * 1000; },
            set onEnd(fn) { _onEnded = fn; }
        };
        """
    )

    AudioPlayer.init()

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
