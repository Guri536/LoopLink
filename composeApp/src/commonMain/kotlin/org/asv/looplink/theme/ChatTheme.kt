package org.asv.looplink.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.asv.looplink.components.chat.RefColors
import org.asv.looplink.ui.adaptiveTextColor
import org.asv.looplink.ui.angledLinearGradientBrush
import org.asv.looplink.ui.lightenHSL

@Serializable
data class ChatTheme(
    val backGroundColorArgb: Int = Colors.DarkColorScheme.background.toArgb(),
    val myMessageBackgroundColorArgb: Int = RefColors.MY_MESSAGE.toArgb(),
    val peerMessageBackgroundColorArgb: Int = RefColors.OTHERS_MESSAGE.toArgb(),
    val myTextColorArgb: Int? = Color.Black.toArgb(),
    val peerTextColorArgb: Int? = Color.Black.toArgb(),
    val backgroundGradientAngle: Float? = null,
    val backgroundGradientArgb: List<Int>? = null,
    val backgroundImagePath: String? = null,
) {

    @Transient
    val backgroundColor: Color = Color(backGroundColorArgb)

    @Transient
    val myMessageColor: Color = Color(myMessageBackgroundColorArgb)

    @Transient
    val peerMessageColor: Color = Color(peerMessageBackgroundColorArgb)

    @Transient
    val myTextColor: Color = myTextColorArgb?.let { Color(it) } ?: myMessageColor.adaptiveTextColor()

    @Transient
    val peerTextColor: Color = peerTextColorArgb?.let { Color(it) } ?: peerMessageColor.adaptiveTextColor()

    @Transient
    val backgroundBrush: Brush? = backgroundGradientArgb?.let { colors ->
        val colorStops = colors.map { Color(it) }
        if (backgroundGradientAngle != null) {
            Brush.angledLinearGradientBrush(
                colorStops,
                backgroundGradientAngle
            )
        } else {
            Brush.linearGradient(colors = colorStops)
        }
    }

    @Transient
    val topBarColor: Color = backgroundColor.lightenHSL(0.2f)

    @Transient
    val topBarTextColor: Color = topBarColor.adaptiveTextColor()
    companion object {
        fun default(): ChatTheme = ChatTheme()
    }
}

