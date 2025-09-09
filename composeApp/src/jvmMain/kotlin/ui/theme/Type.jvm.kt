package ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.platform.Font

actual fun resource(path: String): Font {
    return Font(path)
}