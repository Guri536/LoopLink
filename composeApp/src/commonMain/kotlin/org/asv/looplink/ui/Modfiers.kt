package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.awt.Color as AwtColor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable
expect fun Modifier.backdropBlur(radius: Dp): Modifier


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

@Composable
fun angledLinearGradientBrush(
    colors: List<Color>,
    angle: Float,
    stops: List<Float>? = null
): Brush {
    // The composable version now reuses the core logic and just adds remembering.
    return remember(colors, angle, stops) {
        Brush.angledLinearGradientBrush(colors, angle, stops)
    }
}

private fun hue2rgb(p: Float, q: Float, t: Float): Float {
    var tt = t
    if (tt < 0) tt += 1f
    if (tt > 1) tt -= 1f
    return when {
        tt < 1f / 6f -> p + (q - p) * 6f * tt
        tt < 1f / 2f -> q
        tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
        else -> p
    }
}

fun Color.changeBrightness(amount: Float, getter: (Float, Float) -> Float): Color {
    val r = this.red
    val g = this.green
    val b = this.blue

    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    var h: Float
    var s: Float
    var l = (max + min) / 2f

    if (max == min) {
        h = 0f
        s = 0f
    } else {
        val d = max - min
        s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        h = when (max) {
            r -> (g - b) / d + (if (g < b) 6 else 0)
            g -> (b - r) / d + 2
            else -> (r - g) / d + 4
        }
        h /= 6f
    }

    val normalizedAmount = amount.coerceIn(0f, 1f)
    l = getter(l, normalizedAmount)

    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q
    val rr = hue2rgb(p, q, h + 1f / 3f)
    val gg = hue2rgb(p, q, h)
    val bb = hue2rgb(p, q, h - 1f / 3f)

    return Color(rr, gg, bb, this.alpha)
}

/**
 * Lightens a color by a given amount.
 *
 * @param amount The amount to lighten, from 0.0f to 1.0f. A value of 0.1f makes it 10% lighter.
 * @return The new, lightened [Color].
 */
fun Color.lightenHSL(amount: Float): Color {
    return this.changeBrightness(amount) { it, fraction -> (it + fraction).coerceIn(0f, 1f) }
}

/**
 * Darkens a color by a given amount.
 *
 * @param amount The amount to darken, from 0.0f to 1.0f. A value of 0.1f makes it 10% darker.
 * @return The new, darkened [Color].
 */
fun Color.darkenHSL(amount: Float): Color {
    return this.changeBrightness(amount) { it, fraction -> (it - fraction).coerceIn(0f, 1f) }
}

fun Color.bestTextColor(): Color {
    // Relative luminance formula (WCAG standard)
    val luminance = (0.299f * red + 0.587f * green + 0.114f * blue)

    return if (luminance > 0.5f) Color.Black else Color.White
}

private fun Color.relativeLuminance(): Float {
    fun channel(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

    return 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)
}

/**
 * Returns the text color (white or black) that ensures maximum contrast
 * per WCAG 2.1 relative luminance formula.
 */
fun Color.bestTextColorAccurate(): Color {
    val bgLum = relativeLuminance()
    val whiteContrast = (1.05f) / (bgLum + 0.05f)
    val blackContrast = (bgLum + 0.05f) / 0.05f

    return if (whiteContrast > blackContrast) Color.White else Color.Black
}

fun Color.adaptiveTextColor(): Color {
    // Step 1: Gamma-corrected relative luminance
    fun channel(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

    val luminance = 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)

    // Step 2: Perceptual bias — reds and blues are darker than they look
    // Push the midpoint lower so strong hues lean toward lighter text
    val bias = 0.4f
    val adjusted = (luminance - bias).coerceIn(0f, 1f)

    // Step 3: Map brightness to grayscale value (lighter bg → darker text)
    val textGray = if (adjusted > 0.5f) {
        // On lighter backgrounds → darker text
        (1f - adjusted) * 0.9f
    } else {
        // On darker backgrounds → lighter text
        0.9f
    }

    return Color(textGray, textGray, textGray, 1f)
}