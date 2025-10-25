package org.asv.looplink.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates a [Brush] that draws a linear gradient at a specified angle.
 *
 * This function calculates the start and end offsets of the gradient vector
 * to ensure the gradient spans the entire bounds of the Composable,
 * respecting the given angle.
 *
 * @param colors The list of colors to be used in the gradient.
 * @param angle The angle of the gradient in degrees. 0 degrees is horizontal (left to right),
 * 90 degrees is vertical (top to bottom).
 * @param stops A list of-stop values from 0.0 to 1.0. If null, the colors are
 * distributed evenly.
 * @return A [Brush] that can be used in modifiers like `background`.
 */

fun Brush.Companion.angledLinearGradientBrush(
    colors: List<Color>,
    angle: Float,
    stops: List<Float>? = null
): Brush {

    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            // Convert the angle to radians in a KMP-safe way
            val angleRad = (angle * (PI / 180.0)).toFloat()

            // Calculate the cosine and sine of the angle.
            val cos = cos(angleRad)
            val sin = sin(angleRad)

            // Get the width and height of the Composable.
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            val p00 = 0f // (0, 0) . (cos, sin) = 0
            val pW0 = w * cos
            val p0H = h * sin
            val pWH = w * cos + h * sin

            // Find the minimum and maximum projections.
            val pMin = minOf(p00, pW0, p0H, pWH)
            val pMax = maxOf(p00, pW0, p0H, pWH)

            // Calculate the projection of the center point.
            val pCenter = (w / 2f) * cos + (h / 2f) * sin

            val start = Offset(
                x = center.x + (pMin - pCenter) * cos,
                y = center.y + (pMin - pCenter) * sin
            )

            val end = Offset(
                x = center.x + (pMax - pCenter) * cos,
                y = center.y + (pMax - pCenter) * sin
            )

            return LinearGradientShader(
                colors = colors,
                colorStops = stops,
                from = start,
                to = end,
                tileMode = TileMode.Clamp
            )
        }
    }
}

