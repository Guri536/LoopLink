package org.asv.looplink

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.asv.looplink.components.LocalChatViewModel
import org.asv.looplink.components.LocalCuimsApi
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LocalMainViewModel
import org.asv.looplink.components.LocalPeerDiscoveryViewModel
import org.asv.looplink.components.loadUserInfo
import org.asv.looplink.components.userInfo
import org.asv.looplink.network.AndroidKtorServer
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI

sealed class P2PState {
    object Stopped : P2PState()
    data class Running(val uid: String, val name: String) : P2PState()
}

actual class MainViewModel(applicationContext: Context) : ViewModel() {
    val serverManager: AndroidKtorServer
    val database: DatabaseMng
    val cuimsAPI: cuimsAPI
    var lanServiceDiscovery: LANServiceDiscovery

    val chatViewModel: ChatViewModel
    var peerDiscoveryViewModel: PeerDiscoveryViewModel? = null

    private val _p2pState = MutableStateFlow<P2PState>(P2PState.Stopped)
    val p2pState = _p2pState.asStateFlow()

    init {
        println("MainViewModel: Initializing...")
        serverManager = AndroidKtorServer(applicationContext)
        database = DatabaseMng(DriverFactory(applicationContext).createDriver())
        cuimsAPI = cuimsAPI(WebView(applicationContext))
        lanServiceDiscovery = LANServiceDiscovery(applicationContext)
        chatViewModel = ChatViewModel()

        // Check if user is already logged in
        val userData = database.getUserData()
        if (userData.uid != null) {
            loadUserInfo(database)
            startP2PServices()
        }
    }

    actual fun startP2PServices() {
        if (_p2pState.value is P2PState.Running) return

        println("MainViewModel: Starting P2P services...")

        peerDiscoveryViewModel = PeerDiscoveryViewModel(
            lanServiceDiscovery,
            chatViewModel,
            viewModelScope,
            userInfo.uid!!,
            userInfo.name!!
        )

        viewModelScope.launch {
            serverManager.start(
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
        println("MainViewModel: Stopping P2P services...")
        serverManager.close()
        if (peerDiscoveryViewModel != null) {
            peerDiscoveryViewModel?.stopDiscovery()
        }
        _p2pState.value = P2PState.Stopped
    }

    override fun onCleared() {
        println("MainViewModel: Clearing...")
        stopP2PServices()
        lanServiceDiscovery.close()
        cuimsAPI.destroySession()
        super.onCleared()
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(applicationContext) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val onLoginSuccess = {
            viewModel.startP2PServices()
        }

        setContent()
        {
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
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
