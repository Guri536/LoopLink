package org.asv.looplink

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.HttpClient
import app.

fun main() = application {

    launch{

    }

    val database = DatabaseMng(DriverFactory().createDriver())

    Window(
        onCloseRequest = ::exitApplication,
        title = "LoopLink",
    ) {
        App(database)
    }
}