package org.asv.looplink

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.asv.looplink.network.discovery.LANServiceDiscovery // For client-side discovery
import org.asv.looplink.network.jvmKtorServerRunner
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
    )
    val database = DatabaseMng(DriverFactory().createDriver())
    val cuimsApi = cuimsAPI()

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val clientLanDiscovery = LANServiceDiscovery().apply { initialize() }
    val peerDiscoveryViewModel = PeerDiscoveryViewModel(
        serviceDiscovery = clientLanDiscovery,
        externalScope = applicationScope
    )

    println("Attempting to start JVM Ktor Server")
    jvmKtorServerRunner.start(
        port = 8080,
//        serviceDiscovery = clientLanDiscovery
    )

    Window(
        onCloseRequest = {
            println("Window close requested. Cleaning up...")
            jvmKtorServerRunner.stop()
//            jvmKtorServerRunner.closeDiscovery()

            peerDiscoveryViewModel.clear()
            clientLanDiscovery.close() // Close the client's LANServiceDiscovery
            // Cancel the main application scope for the view model
            applicationScope.cancel("Application closing")

            cuimsApi.destroySession()
            exitApplication()
        },
        state = windowState,
        title = "LoopLink",
        icon = painterResource("icons/icon.svg")
    ) {
        App(database, cuimsApi, peerDiscoveryViewModel)
    }

    // JVM Shutdown Hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("JVM Shutdown Hook: Cleaning up...")
        if (jvmKtorServerRunner.isRunning()) {
            jvmKtorServerRunner.stop()
//            jvmKtorServerRunner.closeDiscovery()
        }
        peerDiscoveryViewModel.clear()
        clientLanDiscovery.close()
        if (applicationScope.isActive) {
            applicationScope.cancel("JVM shutting down")
        }
        cuimsApi.destroySession()
        println("JVM Shutdown Hook: Cleanup complete.")
    })
}
