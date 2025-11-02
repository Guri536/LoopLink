package org.asv.looplink.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

@Composable
actual fun Modifier.backdropBlur(radius: Dp): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Use the platform RenderEffect
        this.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
            renderEffect = RenderEffect
                .createBlurEffect(
                    radius.toPx(),
                    radius.toPx(),
                    Shader.TileMode.CLAMP
                )
                .asComposeRenderEffect()
        }
    } else {
        // No-op for older Android versions
        this
    }
}