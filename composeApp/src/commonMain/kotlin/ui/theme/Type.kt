package ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.platform.Font

val RobotFont = FontFamily(
    Font(resource = "font/roboto-regular.ttf", FontWeight.Normal),
    Font(resource = "font/roboto_italic.ttf", FontWeight.Normal, FontStyle.Italic),
    Font(resource = "font/roboto_bold.ttf", FontWeight.Bold),
    Font(resource = "font/roboto_boldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
    Font(resource = "font/roboto_medium.ttf", FontWeight.Medium),
    Font(resource = "font/roboto_mediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
    Font(resource = "font/roboto_light.ttf", FontWeight.Light),
    Font(resource = "font/roboto_lightItalic.ttf", FontWeight.Light, FontStyle.Italic),
    Font(resource = "font/roboto_thin.ttf", FontWeight.Thin),
    Font(resource = "font/roboto_thinItalic.ttf", FontWeight.Thin, FontStyle.Italic),
    Font(resource = "font/roboto_black.ttf", FontWeight.Black),
    Font(resource = "font/roboto_blackitalic.ttf", FontWeight.Black, FontStyle.Italic),
)

val StoryScriptFont = FontFamily(
    Font(resource = "font/storyscript_regular.ttf", FontWeight.Normal),
)

expect fun resource(path: String): Font