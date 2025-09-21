package org.asv.looplink.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun GetProfileImage(bytes: ByteArray?, modifier: Modifier) {
    if(bytes == null) return
    Image(
        org.jetbrains.skia.Image
            .makeFromEncoded(bytes)
            .toComposeImageBitmap(),
        contentDescription = "Profile Picture",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}