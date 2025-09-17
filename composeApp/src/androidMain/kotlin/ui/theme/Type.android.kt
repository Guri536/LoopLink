package ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import org.asv.looplink.R

actual fun resource(
    path: String,
    weight: FontWeight,
    style: FontStyle
): Font {
    return Font(
        when (path) {
            "font/roboto_black.ttf" ->
                R.font.roboto_black

            "font/roboto_blackitalic.ttf" ->
                R.font.roboto_blackitalic

            "font/roboto_bold.ttf" ->
                R.font.roboto_bold

            "font/roboto_bolditalic.ttf" ->
                R.font.roboto_bolditalic

            "font/roboto_italic.ttf" ->
                R.font.roboto_italic

            "font/roboto_light.ttf" ->
                R.font.roboto_light

            "font/roboto_lightitalic.ttf" ->
                R.font.roboto_lightitalic

            "font/roboto_medium.ttf" ->
                R.font.roboto_medium

            "font/roboto_mediumitalic.ttf" ->
                R.font.roboto_mediumitalic

            "font/roboto_regular.ttf" ->
                R.font.roboto_regular

            "font/roboto_thin.ttf" ->
                R.font.roboto_thin

            "font/roboto_thinitalic.ttf" ->
                R.font.roboto_thinitalic

            "font/storyscript_regular.ttf" ->
                R.font.storyscript_regular

            else -> error("No font found with path: $path")
        },
        weight,
        style
    )
}
