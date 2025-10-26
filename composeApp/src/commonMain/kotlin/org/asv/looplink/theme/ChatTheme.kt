package org.asv.looplink.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.asv.looplink.components.chat.RefColors
import org.asv.looplink.typesalias.KmpIgnoreOnParcel
import org.asv.looplink.typesalias.KmpParcelable
import org.asv.looplink.typesalias.KmpParcelize
import org.asv.looplink.ui.adaptiveTextColor
import org.asv.looplink.ui.angledLinearGradientBrush
import org.asv.looplink.ui.lightenHSL

expect class ChatTheme(
    backGroundColorArgb: Int = Colors.DarkColorScheme.background.toArgb(),
    myMessageBackgroundColorArgb: Int = RefColors.MY_MESSAGE.toArgb(),
    peerMessageBackgroundColorArgb: Int = RefColors.OTHERS_MESSAGE.toArgb(),
    myTextColorArgb: Int? = Color.Black.toArgb(),
    peerTextColorArgb: Int? = Color.Black.toArgb(),
    backgroundGradientAngle: Float? = null,
    backgroundGradientArgb: List<Int>? = null,
    backgroundImagePath: String? = null,
): KmpParcelable {
    val backgroundColor: Color
    val myMessageColor: Color
    val peerMessageColor: Color
    val myTextColor: Color
    val peerTextColor: Color
    val backgroundBrush: Brush?
    val topBarColor: Color
    val topBarTextColor: Color

    companion object {
        fun default(): ChatTheme
    }

    val backGroundColorArgb: Int
    val myMessageBackgroundColorArgb: Int
    val peerMessageBackgroundColorArgb: Int
    val myTextColorArgb: Int?
    val peerTextColorArgb: Int?
    val backgroundGradientAngle: Float?
    val backgroundGradientArgb: List<Int>?
    val backgroundImagePath: String?

    fun copyMe(
        backGroundColorArgb: Int = this.backGroundColorArgb,
        myMessageBackgroundColorArgb: Int = this.myMessageBackgroundColorArgb,
        peerMessageBackgroundColorArgb: Int = this.peerMessageBackgroundColorArgb,
        myTextColorArgb: Int? = this.myTextColorArgb,
        peerTextColorArgb: Int? = this.peerTextColorArgb,
        backgroundGradientAngle: Float? = this.backgroundGradientAngle,
        backgroundGradientArgb: List<Int>? = this.backgroundGradientArgb,
        backgroundImagePath: String? = this.backgroundImagePath,
    ): ChatTheme
}
