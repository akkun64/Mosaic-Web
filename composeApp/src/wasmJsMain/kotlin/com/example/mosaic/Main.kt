package com.example.mosaic

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    js(
        """
        (function() {
            var _audio = new Audio();
            var _onEnded = null;
            window._mosaicFiles = [];
            _audio.addEventListener('ended', function() { if (_onEnded) _onEnded(); });
            window._mosaicInit = function() {};
            window._mosaicLoadByIndex = function(idx) {
                if (_audio.src) URL.revokeObjectURL(_audio.src);
                _audio.src = URL.createObjectURL(window._mosaicFiles[idx]);
                _audio.load();
            };
            window._mosaicPlay = function() { _audio.play(); };
            window._mosaicPause = function() { _audio.pause(); };
            window._mosaicSeek = function(ms) { _audio.currentTime = ms / 1000; };
            window._mosaicGetTime = function() { return (_audio.currentTime || 0) * 1000; };
            window._mosaicGetDur = function() { return (_audio.duration || 0) * 1000; };
            window._mosaicSetOnEnd = function(fn) { _onEnded = fn; };
            window._mosaicOpenPicker = function(callback) {
                var input = document.createElement('input');
                input.type = 'file';
                input.multiple = true;
                input.accept = 'audio/*';
                input.onchange = function() {
                    window._mosaicFiles = [];
                    for (var i = 0; i < input.files.length; i++) {
                        window._mosaicFiles.push(input.files[i]);
                    }
                    callback();
                };
                input.click();
            };
        })()
        """
    )

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
