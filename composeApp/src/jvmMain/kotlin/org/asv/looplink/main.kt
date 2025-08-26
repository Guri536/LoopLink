package org.asv.looplink

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val database = DatabaseMng(DriverFactory().createDriver())

    Window(
        onCloseRequest = ::exitApplication,
        title = "LoopLink",
    ) {
        App(database)
    }
}