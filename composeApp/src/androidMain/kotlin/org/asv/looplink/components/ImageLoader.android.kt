package org.asv.looplink.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil3.request.ImageResult
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
internal actual fun painterFromFile(path: String): Painter? {
    val context = LocalContext.current

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(path.toUri())
            .crossfade(true)
            .build()
    )

}