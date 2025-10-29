package org.asv.looplink.operations

import androidx.compose.ui.graphics.ImageBitmap
import org.asv.looplink.webDriver.toImageBitmap
import java.io.File

actual fun getBytesFromFile(filePath: String): ImageBitmap? {
    try {
        val file = File(filePath)
        if(!file.exists() || !file.canRead()) {
            println("Cannot fetch file, as it doesn't exist or unreadable")
            return null
        }
        return file.readBytes().toImageBitmap()
    } catch (e: Exception){
        println("An error occurred while reading file: ${e.message}")
        return null
    }
}