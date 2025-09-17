package ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle

val RobotFont = FontFamily(
    resource("font/roboto_regular.ttf"),
    resource("font/roboto_italic.ttf", FontWeight.Normal, FontStyle.Italic),
    resource("font/roboto_bold.ttf", FontWeight.Bold),
    resource("font/roboto_bolditalic.ttf", FontWeight.Bold, FontStyle.Italic),
    resource("font/roboto_medium.ttf", FontWeight.Medium),
    resource("font/roboto_mediumitalic.ttf", FontWeight.Medium, FontStyle.Italic),
    resource("font/roboto_light.ttf", FontWeight.Light),
    resource("font/roboto_lightitalic.ttf", FontWeight.Light, FontStyle.Italic),
    resource("font/roboto_thin.ttf", FontWeight.Thin),
    resource("font/roboto_thinitalic.ttf", FontWeight.Thin, FontStyle.Italic),
    resource("font/roboto_black.ttf", FontWeight.Black),
    resource("font/roboto_blackitalic.ttf", FontWeight.Black, FontStyle.Italic),
)

expect fun resource(
    path: String,
    weight: FontWeight= FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font