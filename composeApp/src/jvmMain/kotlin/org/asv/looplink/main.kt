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

fun main() = application {
    val windowState = rememberWindowState()
    val database = DatabaseMng(DriverFactory().createDriver())

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
    ) {
        App(database)
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down JVM Ktor Server")
        if (jvmKtorServerRunner.isRunning()) {
            jvmKtorServerRunner.stop()
            jvmKtorServerRunner.closeDiscovery()
        }
    })
}