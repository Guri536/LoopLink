package org.asv.looplink.components.chat

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle

@OptIn(ExperimentalTextApi::class)
actual fun getPlatformTextStyle(): PlatformTextStyle {
     return PlatformTextStyle(
         spanStyle = null,
         paragraphStyle = null
     )
}

