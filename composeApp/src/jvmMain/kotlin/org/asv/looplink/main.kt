package org.asv.looplink

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import looplink.composeapp.generated.resources.Res
import looplink.composeapp.generated.resources.iconSVG
import org.asv.looplink.components.loadUserInfo
import org.asv.looplink.components.userInfo
import org.asv.looplink.di.initKoin
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.jvmKtorServerRunner
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI
import org.jetbrains.compose.resources.painterResource
import org.koin.java.KoinJavaComponent.inject


fun main() = application {
    initKoin()

    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
    )

    val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val viewModel: MainViewModel by inject(MainViewModel::class.java)

    val onLoginSuccess = {
        viewModel.startP2PServices()
    }

    Window(
        onCloseRequest = {
            println("Window close requested. Cleaning up...")
            jvmKtorServerRunner.stop()

            viewModel.peerDiscoveryViewModel.value?.clear()
            viewModel.lanServiceDiscovery.close() // Close the client's LANServiceDiscovery
            // Cancel the main application scope for the view model
            applicationScope.cancel("Application closing")

            viewModel.cuimsAPI.destroySession()
            exitApplication()
        },
        state = windowState,
        title = "LoopLink",
        icon = painterResource(Res.drawable.iconSVG)
    ) {
        App(
            onLoginSuccess
        )
    }

    // JVM Shutdown Hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("JVM Shutdown Hook: Cleaning up...")
        if (jvmKtorServerRunner.isRunning()) {
            jvmKtorServerRunner.stop()
        }
        viewModel.peerDiscoveryViewModel.value?.clear()
        viewModel.lanServiceDiscovery.close()
        if (applicationScope.isActive) {
            applicationScope.cancel("JVM shutting down")
        }
        viewModel.cuimsAPI.destroySession()
        println("JVM Shutdown Hook: Cleanup complete.")
    })
}
