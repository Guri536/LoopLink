package org.asv.looplink.components.chat

import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle

actual fun getPlatformTextStyle(): PlatformTextStyle {
    return PlatformTextStyle(EmojiSupportMatch.None)
}

actual fun openEmojiPanel(x: Int, y: Int) {
}