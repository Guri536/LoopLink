package org.asv.looplink

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import looplink.composeapp.generated.resources.Res
import looplink.composeapp.generated.resources.iconSVG
import org.asv.looplink.di.commonModule
import org.asv.looplink.di.platformModule
import org.asv.looplink.network.jvmKtorServerRunner
import org.asv.looplink.viewmodel.MainViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.get


fun main() {
    startKoin {
        modules(commonModule, platformModule())
    }
    application {

        val windowState = rememberWindowState(
            placement = WindowPlacement.Maximized,
        )

        val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val viewModel: MainViewModel = get(MainViewModel::class.java)
        val onLoginSuccess = {
            viewModel.startP2PServices()
        }
        var isWindowVisible by remember { mutableStateOf(true) }

        if (isWindowVisible) {
            Window(
                onCloseRequest = {
                    isWindowVisible = false
                },
                state = windowState,
                title = "LoopLink",
                icon = painterResource(Res.drawable.iconSVG),
            ) {
                App(
                    onLoginSuccess
                )

            }
        }
        Tray(
            icon = painterResource(Res.drawable.iconSVG),
            state = rememberTrayState(),
            tooltip = "LoopLink",
            onAction = {
                isWindowVisible = true
            },
            menu = {
                Item(
                    "Open Looplink",
                    onClick = { isWindowVisible = true }
                )
                Separator()
                Item(
                    "Exit",
                    onClick = {
                        viewModel.stopP2PServices()
                        exitApplication()
                    }
                )
            }
        )

        // JVM Shutdown Hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            println("JVM Shutdown Hook: Cleaning up...")
            stopKoin()
            if (jvmKtorServerRunner.isRunning()) {
                jvmKtorServerRunner.stop()
            }
            viewModel.stopP2PServices()
            viewModel.peerDiscoveryViewModel.clear()
            viewModel.lanServiceDiscovery.close()
            if (applicationScope.isActive) {
                applicationScope.cancel("JVM shutting down")
            }
            viewModel.cuimsAPI.destroySession()
            println("JVM Shutdown Hook: Cleanup complete.")
        })
    }
}