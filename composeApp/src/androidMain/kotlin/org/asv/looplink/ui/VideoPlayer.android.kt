package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.request.Disposable

//@Composable
//actual fun VideoPlayer(modifier: Modifier, filePath: String) {
//    val context = LocalContext.current
//    val exoPlayer = remember{
//        ExoPlayer.Builder(context).build().apply {
//            setMediaItem(MediaItem.fromUri("file://$filePath"))
//            prepare()
//        }
//    }
//
//    AndroidView(
//        factory = { PlayerView(it).apply { player = exoPlayer } },
//        modifier = modifier
//    )
//
//    DisposableEffect(Unit){
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//}