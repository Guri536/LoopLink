package ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual fun resource(
    path: String,
    weight: FontWeight,
    style: FontStyle
): Font {
    return Font(path, weight, style)
}