package org.asv.looplink.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.res.loadImageBitmap
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO
import coil3.compose.rememberAsyncImagePainter
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.InputStream

@Composable
internal actual fun painterFromFile(path: String): Painter? {
    return remember(path) {
        try {
            FileInputStream(File(path)).use { input ->
                BitmapPainter(input.readAllBytes().decodeToImageBitmap())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}