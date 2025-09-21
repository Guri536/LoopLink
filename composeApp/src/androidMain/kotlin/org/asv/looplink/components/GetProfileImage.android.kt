package org.asv.looplink.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun GetProfileImage(
    bytes: ByteArray?,
    modifier: Modifier
) {
    if (bytes == null) return

    Image(
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap(),
        contentDescription = "Profile Picture",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}