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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

val Blue = Color(0xFF448AFF)
val BgBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF1A1A1A)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF999999)
val DividerColor = Color(0xFF2A2A2A)

data class Song(val index: Int, val name: String)

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

        AudioPlayer.setOnEnded {
            isPlaying = false
            if (repeatOn && currentIndex >= 0) {
                AudioPlayer.play()
                isPlaying = true
            } else if (currentIndex >= 0) {
                val next = if (shuffleOn) {
                    songs.indices.random()
                } else {
                    (currentIndex + 1) % songs.size
                }
                currentIndex = next
                AudioPlayer.loadByIndex(songs[next].index)
                AudioPlayer.play()
                isPlaying = true
            }
        }

        if (songs.isEmpty()) {
            WelcomeScreen {
                songs = it
                if (it.isNotEmpty()) {
                    currentIndex = 0
                    AudioPlayer.loadByIndex(it[0].index)
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
                    AudioPlayer.loadByIndex(songs[next].index)
                    AudioPlayer.play()
                    isPlaying = true
                    currentTime = 0f
                },
                onPrev = {
                    if (currentTime > 3000f) {
                        AudioPlayer.seek(0.0)
                        currentTime = 0f
                    } else {
                        val prev = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
                        currentIndex = prev
                        AudioPlayer.loadByIndex(songs[prev].index)
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
                        AudioPlayer.loadByIndex(songs[idx].index)
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
                    AudioPlayer.loadByIndex(songs[idx].index)
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
                        AudioPlayer.loadByIndex(songs[next].index)
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
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Blue, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("MUSIC", color = Blue, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text("Select audio files to play", color = TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    AudioPlayer.openFilePicker {
                        val count = AudioPlayer.getFileCount()
                        val result = (0 until count).map { i ->
                            Song(i, AudioPlayer.getFileName(i))
                        }
                        if (result.isNotEmpty()) onLoad(result)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
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
        Box(Modifier.fillMaxWidth().background(SurfaceDark).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("MUSIC", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
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
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = TextGray, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.name, color = if (isCurrent) Blue else TextWhite, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Divider(color = DividerColor, thickness = 0.5.dp)
            }
        }

        if (currentSong != null) {
            Divider(color = DividerColor, thickness = 1.dp)
            Row(
                Modifier.fillMaxWidth().background(SurfaceDark)
                    .clickable { onMiniTap() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(DividerColor), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(currentSong.name, color = TextWhite, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onMiniPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null, tint = TextWhite, modifier = Modifier.size(20.dp))
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
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Back", tint = TextWhite, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.weight(1f))
        }

        Box(
            Modifier.fillMaxWidth().weight(0.45f).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.aspectRatio(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(SurfaceDark), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Album, contentDescription = null, tint = TextGray, modifier = Modifier.size(80.dp))
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val displayName = if (song.name.contains(".")) song.name.substringBeforeLast(".") else song.name
            Text(displayName, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }

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

        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle", tint = if (shuffleOn) Blue else TextGray, modifier = Modifier.size(22.dp).clickable { onToggleShuffle() })
            Icon(Icons.Filled.FavoriteBorder, contentDescription = "Favorite", tint = TextGray, modifier = Modifier.size(22.dp))
            Icon(Icons.Filled.Equalizer, contentDescription = "Equalizer", tint = TextGray, modifier = Modifier.size(22.dp))
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.QueueMusic, contentDescription = "Queue", tint = TextGray, modifier = Modifier.size(24.dp))
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = TextWhite, modifier = Modifier.size(32.dp).clickable { onPrev() })
            Box(Modifier.size(60.dp).clip(CircleShape).background(Blue).clickable { onPlayPause() }, contentAlignment = Alignment.Center) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = TextWhite, modifier = Modifier.size(32.dp).clickable { onNext() })
            Icon(Icons.Filled.Repeat, contentDescription = "Repeat", tint = if (repeatOn) Blue else TextGray, modifier = Modifier.size(22.dp).clickable { onToggleRepeat() })
        }
    }
}

fun formatMs(ms: Double): String {
    val s = (ms / 1000).toInt()
    val min = s / 60
    val sec = s % 60
    return "$min:${if (sec < 10) "0$sec" else "$sec"}"
}
