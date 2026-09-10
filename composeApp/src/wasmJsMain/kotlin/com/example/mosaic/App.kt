package com.example.mosaic

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.browser.document
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get as fileGet
import kotlin.js.Promise

val Blue = Color(0xFF448AFF)
val BgBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF1A1A1A)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF999999)
val DividerColor = Color(0xFF2A2A2A)

data class Song(
    val name: String,
    val file: File
)

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme(
        background = BgBlack,
        surface = SurfaceDark,
        primary = Blue,
        onPrimary = TextWhite,
        onBackground = TextWhite,
        onSurface = TextWhite
    )) {
        var songs by remember { mutableStateOf(listOf<Song>()) }
        var currentIndex by remember { mutableIntStateOf(-1) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentTime by remember { mutableFloatStateOf(0f) }
        var duration by remember { mutableFloatStateOf(0f) }
        var showPlayer by remember { mutableStateOf(false) }
        var shuffleOn by remember { mutableStateOf(false) }
        var repeatOn by remember { mutableStateOf(false) }

        val currentSong = songs.getOrNull(currentIndex)

        LaunchedEffect(isPlaying, currentIndex) {
            if (currentIndex < 0) return@LaunchedEffect
            while (isPlaying) {
                kotlinx.coroutines.delay(500)
                currentTime = AudioPlayer.getCurrentTime().toFloat()
                duration = AudioPlayer.getDuration().toFloat()
            }
        }

        AudioPlayer.onEnded = {
            isPlaying = false
            if (repeatOn && currentIndex >= 0) {
                AudioPlayer.play()
                isPlaying = true
            } else if (currentIndex >= 0) {
                val next = if (shuffleOn) {
                    (songs.indices).random()
                } else {
                    (currentIndex + 1) % songs.size
                }
                currentIndex = next
                AudioPlayer.load(songs[next].file)
                AudioPlayer.play()
                isPlaying = true
            }
        }

        if (songs.isEmpty()) {
            WelcomeScreen {
                songs = it
                if (it.isNotEmpty()) {
                    currentIndex = 0
                    AudioPlayer.load(it[0].file)
                }
            }
        } else if (showPlayer && currentSong != null) {
            NowPlayingScreen(
                song = currentSong,
                songs = songs,
                currentIndex = currentIndex,
                isPlaying = isPlaying,
                currentTime = currentTime,
                duration = duration,
                shuffleOn = shuffleOn,
                repeatOn = repeatOn,
                onBack = { showPlayer = false },
                onPlayPause = {
                    if (isPlaying) AudioPlayer.pause() else AudioPlayer.play()
                    isPlaying = !isPlaying
                },
                onNext = {
                    val next = if (shuffleOn) (songs.indices).random() else (currentIndex + 1) % songs.size
                    currentIndex = next
                    AudioPlayer.load(songs[next].file)
                    AudioPlayer.play()
                    isPlaying = true
                    currentTime = 0f
                },
                onPrev = {
                    if (currentTime > 3000) {
                        AudioPlayer.seek(0)
                        currentTime = 0f
                    } else {
                        val prev = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
                        currentIndex = prev
                        AudioPlayer.load(songs[prev].file)
                        AudioPlayer.play()
                        isPlaying = true
                        currentTime = 0f
                    }
                },
                onSeek = { AudioPlayer.seek(it.toDouble()) },
                onToggleShuffle = { shuffleOn = !shuffleOn },
                onToggleRepeat = { repeatOn = !repeatOn },
                onPageChange = { idx ->
                    if (idx != currentIndex) {
                        currentIndex = idx
                        AudioPlayer.load(songs[idx].file)
                        AudioPlayer.play()
                        isPlaying = true
                        currentTime = 0f
                    }
                }
            )
        } else {
            LibraryScreen(
                songs = songs,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onSongClick = { idx ->
                    currentIndex = idx
                    AudioPlayer.load(songs[idx].file)
                    AudioPlayer.play()
                    isPlaying = true
                    currentTime = 0f
                    showPlayer = true
                },
                onMiniPlayPause = {
                    if (isPlaying) AudioPlayer.pause() else AudioPlayer.play()
                    isPlaying = !isPlaying
                },
                onNext = {
                    if (currentIndex >= 0) {
                        val next = (currentIndex + 1) % songs.size
                        currentIndex = next
                        AudioPlayer.load(songs[next].file)
                        AudioPlayer.play()
                        isPlaying = true
                        currentTime = 0f
                    }
                },
                onMiniTap = { if (currentSong != null) showPlayer = true }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onLoad: (List<Song>) -> Unit) {
    Box(Modifier.fillMaxSize().background(BgBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MUSIC", color = Blue, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text("Drop audio files here", color = TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val input = document.createElement("input") as org.w3c.dom.HTMLInputElement
                    input.type = "file"
                    input.multiple = true
                    input.accept = "audio/*"
                    input.onchange = {
                        val files = input.files
                        val result = mutableListOf<Song>()
                        if (files != null) {
                            for (i in 0 until files.length) {
                                files.fileGet(i)?.let { file ->
                                    result.add(Song(file.name, file))
                                }
                            }
                        }
                        if (result.isNotEmpty()) onLoad(result)
                    }
                    input.click()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text("Select Audio Files", color = TextWhite)
            }
        }
    }
}

@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Int) -> Unit,
    onMiniPlayPause: () -> Unit,
    onNext: () -> Unit,
    onMiniTap: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(BgBlack)) {
        // Header
        Box(Modifier.fillMaxWidth().background(SurfaceDark).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("MUSIC", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
        // Tabs
        Row(Modifier.fillMaxWidth().background(SurfaceDark)) {
            listOf("Tracks").forEach { tab ->
                Text(
                    tab,
                    color = Blue,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Divider(color = DividerColor, thickness = 1.dp)

        // Song list
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(songs) { idx, song ->
                val isCurrent = song == currentSong
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (isCurrent) Blue.copy(alpha = 0.08f) else BgBlack)
                        .clickable { onSongClick(idx) }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)).background(SurfaceDark), contentAlignment = Alignment.Center) {
                        Text("\u266A", color = TextGray, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.name, color = if (isCurrent) Blue else TextWhite, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Divider(color = DividerColor, thickness = 0.5.dp)
            }
        }

        // Mini player
        if (currentSong != null) {
            Divider(color = DividerColor, thickness = 1.dp)
            Row(
                Modifier.fillMaxWidth().background(SurfaceDark)
                    .clickable { onMiniTap() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(DividerColor), contentAlignment = Alignment.Center) {
                    Text("\u266A", color = TextGray, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(currentSong.name, color = TextWhite, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onMiniPlayPause) {
                    Text(if (isPlaying) "\u23F8" else "\u25B6", color = TextWhite, fontSize = 16.sp)
                }
                IconButton(onClick = onNext) {
                    Text("\u23ED", color = TextWhite, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun NowPlayingScreen(
    song: Song,
    songs: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    currentTime: Float,
    duration: Float,
    shuffleOn: Boolean,
    repeatOn: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize().background(BgBlack)) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("\u276E", color = TextWhite, fontSize = 18.sp)
            }
            Spacer(Modifier.weight(1f))
        }

        // Album art placeholder (large)
        Box(
            Modifier.fillMaxWidth().weight(0.45f).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.aspectRatio(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(SurfaceDark), contentAlignment = Alignment.Center) {
                Text("\u266B", color = TextGray, fontSize = 64.sp)
            }
        }

        // Song info
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(song.name.substringBeforeLast("."), color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }

        // Seek bar
        Column(Modifier.padding(horizontal = 24.dp)) {
            Slider(
                value = currentTime,
                onValueChange = { onSeek(it) },
                valueRange = 0f..maxOf(duration, 1f),
                colors = SliderDefaults.colors(
                    thumbColor = Blue,
                    activeTrackColor = Blue
                )
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatMs(currentTime.toDouble()), color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text(formatMs(duration.toDouble()), color = TextGray, fontSize = 12.sp)
            }
        }

        // Middle row
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("\u21C4", color = if (shuffleOn) Blue else TextGray, fontSize = 20.sp, modifier = Modifier.clickable { onToggleShuffle() })
            Text("\u2661", color = TextGray, fontSize = 20.sp)
            Text("\u2630", color = TextGray, fontSize = 20.sp)
        }

        // Bottom controls
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("\u23F7", color = TextGray, fontSize = 20.sp)
            Text("\u23EE", color = TextWhite, fontSize = 28.sp, modifier = Modifier.clickable { onPrev() })
            Box(Modifier.size(60.dp).clip(CircleShape).background(Blue).clickable { onPlayPause() }, contentAlignment = Alignment.Center) {
                Text(if (isPlaying) "\u23F8" else "\u25B6", color = TextWhite, fontSize = 28.sp)
            }
            Text("\u23ED", color = TextWhite, fontSize = 28.sp, modifier = Modifier.clickable { onNext() })
            Text("\u21BB", color = if (repeatOn) Blue else TextGray, fontSize = 20.sp, modifier = Modifier.clickable { onToggleRepeat() })
        }
    }
}

fun formatMs(ms: Double): String {
    val s = (ms / 1000).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}
