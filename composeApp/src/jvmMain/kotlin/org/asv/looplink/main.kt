package org.asv.looplink

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.application
import io.ktor.client.HttpClient
import org.asv.looplink.network.jvmKtorServerRunner // Import the server runner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.WindowPlacement
import org.jetbrains.compose.resources.painterResource
import looplink.composeapp.generated.resources.Res
import org.asv.looplink.webDriver.cuimsAPI

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,

    )
    val database = DatabaseMng(DriverFactory().createDriver())
    val cuimsAPI = cuimsAPI()

    println("Attempting to start JVM Ktor Server")
    jvmKtorServerRunner.start(port = 8080, instanceName = "MyLoopLinkJVMInstance")

    Window(
        onCloseRequest = {
            println("Window close requested")
            jvmKtorServerRunner.stop()
            jvmKtorServerRunner.closeDiscovery()
            exitApplication()
        },
        state = windowState,
        title = "LoopLink",
        icon = painterResource("icons/icon.svg")
    ) {
        App(database, cuimsAPI)
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down JVM Ktor Server")
        if (jvmKtorServerRunner.isRunning()) {
            jvmKtorServerRunner.stop()
            jvmKtorServerRunner.closeDiscovery()
        }
        cuimsAPI.destroySession()
    })
}