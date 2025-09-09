package ui.theme

import androidx.compose.ui.text.font.Font
import org.asv.looplink.R

actual fun resource(path: String): Font {
    return when(path){
        "font/roboto_black.ttf" -> Font(R.font.roboto_black)
        "font/roboto_blackitalic.ttf" -> Font(R.font.roboto_blackitalic)
        "font/roboto_bold.ttf" -> Font(R.font.roboto_bold)
        "font/roboto_bolditalic.ttf" -> Font(R.font.roboto_bolditalic)
        "font/roboto_italic.ttf" -> Font(R.font.roboto_italic)
        "font/roboto_light.ttf" -> Font(R.font.roboto_light)
        "font/roboto_lightitalic.ttf" -> Font(R.font.roboto_lightitalic)
        "font/roboto_medium.ttf" -> Font(R.font.roboto_medium)
        "font/roboto_mediumitalic.ttf" -> Font(R.font.roboto_mediumitalic)
        "font/roboto_regular.ttf" -> Font(R.font.roboto_regular)
        "font/roboto_thin.ttf" -> Font(R.font.roboto_thin)
        "font/roboto_thinitalic.ttf" -> Font(R.font.roboto_thinitalic)
        "font/storyscript_regular.ttf" -> Font(R.font.storyscript_regular)
        else -> error("No font found with path: $path")
    }
}