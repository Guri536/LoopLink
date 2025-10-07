package org.asv.looplink

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
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
import org.asv.looplink.components.LocalChatViewModel
import org.asv.looplink.components.LocalCuimsApi
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LocalMainViewModel
import org.asv.looplink.components.LocalPeerDiscoveryViewModel
import org.asv.looplink.components.userInfo
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.jvmKtorServerRunner
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI

sealed class P2PState {
    object Stopped : P2PState()
    data class Running(val uid: String, val name: String) : P2PState()
}

actual class MainViewModel : ViewModel() {
    val database: DatabaseMng
    val cuimsAPI: cuimsAPI
    val clientLanDiscovery: LANServiceDiscovery
    val chatViewModel: ChatViewModel
    var peerDiscoveryViewModel: PeerDiscoveryViewModel? = null

    private val _p2pState = MutableStateFlow<P2PState>(P2PState.Stopped)
    val p2pState = _p2pState.asStateFlow()

    init {
        println("MainViewModel: Initializing (JVM)")
        database = DatabaseMng(DriverFactory().createDriver())
        cuimsAPI = cuimsAPI()
        clientLanDiscovery = LANServiceDiscovery().apply { initialize() }
        chatViewModel = ChatViewModel()

        val userData = database.getUserData()
        if (userData.uid != null) {
            startP2PServices()
        }
    }

    actual fun startP2PServices() {
        if (_p2pState.value is P2PState.Running) return

        println("MainViewModel: Starting P2P Services")
        peerDiscoveryViewModel = PeerDiscoveryViewModel(
            clientLanDiscovery,
            chatViewModel,
            viewModelScope,
            userInfo.uid!!,
            userInfo.name!!
        )

        viewModelScope.launch {
            jvmKtorServerRunner.start(
                port = 8080,
                userUid = userInfo.uid!!,
                userName = userInfo.name!!,
                chatViewModel = chatViewModel,
                peerDiscoveryViewModel
            )
        }


        _p2pState.value = P2PState.Running(userInfo.uid!!, userInfo.name!!)
    }

    actual fun stopP2PServices() {
        println("MainViewModel: Stopping P2P Services")
        jvmKtorServerRunner.stop()
        if (peerDiscoveryViewModel != null) {
            peerDiscoveryViewModel?.stopDiscovery()
        }
        _p2pState.value = P2PState.Stopped
    }

    override fun onCleared() {
        println("MainViewModel: Clearing")
        stopP2PServices()
        clientLanDiscovery.close()
        cuimsAPI.destroySession()
        super.onCleared()
    }
}

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
    )

    val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val viewModel: MainViewModel = MainViewModel()

    val onLoginSuccess = {
        viewModel.startP2PServices()
    }

    Window(
        onCloseRequest = {
            println("Window close requested. Cleaning up...")
            jvmKtorServerRunner.stop()

            viewModel.peerDiscoveryViewModel?.clear()
            viewModel.clientLanDiscovery.close() // Close the client's LANServiceDiscovery
            // Cancel the main application scope for the view model
            applicationScope.cancel("Application closing")

            viewModel.cuimsAPI.destroySession()
            exitApplication()
        },
        state = windowState,
        title = "LoopLink",
        icon = painterResource("icons/icon.svg")
    ) {
        CompositionLocalProvider(
            LocalMainViewModel provides viewModel,
            LocalDatabase provides viewModel.database,
            LocalCuimsApi provides viewModel.cuimsAPI,
            LocalPeerDiscoveryViewModel provides viewModel.peerDiscoveryViewModel,
            LocalChatViewModel provides viewModel.chatViewModel
        ) {
            App(
                onLoginSuccess
            )
        }
    }

    // JVM Shutdown Hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("JVM Shutdown Hook: Cleaning up...")
        if (jvmKtorServerRunner.isRunning()) {
            jvmKtorServerRunner.stop()
        }
        viewModel.peerDiscoveryViewModel?.clear()
        viewModel.clientLanDiscovery.close()
        if (applicationScope.isActive) {
            applicationScope.cancel("JVM shutting down")
        }
        viewModel.cuimsAPI.destroySession()
        println("JVM Shutdown Hook: Cleanup complete.")
    })
}
