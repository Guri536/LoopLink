package org.asv.looplink.theme

import android.os.Parcelable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.asv.looplink.components.chat.RefColors
import org.asv.looplink.typesalias.KmpParcelable
import org.asv.looplink.ui.adaptiveTextColor
import org.asv.looplink.ui.angledLinearGradientBrush
import org.asv.looplink.ui.lightenHSL

@Parcelize
@Serializable
actual class ChatTheme actual constructor(
    actual val backGroundColorArgb: Int,
    actual val myMessageBackgroundColorArgb: Int,
    actual val peerMessageBackgroundColorArgb: Int,
    actual val myTextColorArgb: Int?,
    actual val peerTextColorArgb: Int?,
    actual val backgroundGradientAngle: Float?,
    actual val backgroundGradientArgb: List<Int>?,
    actual val backgroundImagePath: String?
): Parcelable {
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val backgroundColor: Color = Color(backGroundColorArgb)
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val myMessageColor: Color = Color(myMessageBackgroundColorArgb)
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val peerMessageColor: Color = Color(peerMessageBackgroundColorArgb)
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val myTextColor: Color = myTextColorArgb?.let { Color(it) } ?: myMessageColor.adaptiveTextColor()
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val peerTextColor: Color = peerTextColorArgb?.let { Color(it) } ?: peerMessageColor.adaptiveTextColor()
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val backgroundBrush: Brush? = backgroundGradientArgb?.let { colors ->
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
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val topBarColor: Color = backgroundColor.lightenHSL(0.2f)
    @IgnoredOnParcel
    @Contextual
    @Transient
    actual val topBarTextColor: Color = topBarColor.adaptiveTextColor()
    @Parcelize
    @Serializable
    actual companion object: Parcelable {
        actual fun default(): ChatTheme = ChatTheme()
    }

    actual fun copyMe(
        backGroundColorArgb: Int,
        myMessageBackgroundColorArgb: Int,
        peerMessageBackgroundColorArgb: Int,
        myTextColorArgb: Int?,
        peerTextColorArgb: Int?,
        backgroundGradientAngle: Float?,
        backgroundGradientArgb: List<Int>?,
        backgroundImagePath: String?
    ): ChatTheme {
        return ChatTheme(
            backGroundColorArgb,
            myMessageBackgroundColorArgb,
            peerMessageBackgroundColorArgb,
            myTextColorArgb,
            peerTextColorArgb,
            backgroundGradientAngle,
            backgroundGradientArgb,
            backgroundImagePath
        )
    }
}