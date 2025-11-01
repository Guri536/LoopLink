package org.asv.looplink.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import java.io.File
import chaintech.videoplayer.model.PlayerOption
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.preview.VideoPreviewComposable

//@Composable
//expect fun VideoPlayer(
//    modifier: Modifier = Modifier,
//    filePath: String
//    )

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    filePath: String
){
    val mediaHost = MediaPlayerHost(
        mediaUrl = File(filePath).toString(),
        isPaused = true
    )
    val config = VideoPlayerConfig(

    )
    VideoPlayerComposable(
        modifier = modifier.fillMaxSize(),
        playerHost = mediaHost,
        playerConfig = config
    )
}