package org.asv.looplink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.asv.looplink.components.LocalAppNavigator
import org.koin.java.KoinJavaComponent.get
import org.asv.looplink.components.LoginFields
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.network.ServerManager
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.ui.AppNavigator
import org.asv.looplink.ui.MainScreen
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.compose.koinInject
import ui.theme.AppTheme

sealed class P2PState {
    object Stopped : P2PState()
    data class Running(val uid: String, val name: String) : P2PState()
}

class MainViewModel(
    val database: DatabaseMng,
    val cuimsAPI: cuimsAPI,
    val lanServiceDiscovery: LANServiceDiscovery,
    val chatViewModel: ChatViewModel,
    val peerDiscoveryViewModel: PeerDiscoveryViewModel,
    private val serverManager: ServerManager
) : ViewModel() {
    private val _p2pState = MutableStateFlow<P2PState>(P2PState.Stopped)
    val p2pState = _p2pState.asStateFlow()

    init {
        println("MainViewModel: Initializing...")
        // Check if user is already logged in
        val userData = database.getSize()
        if (userData > 0) {
            get<UserRespository>(UserRespository::class.java).loadUser()
            if(_p2pState.value is P2PState.Stopped) startP2PServices()
        }
    }

    fun startP2PServices() {
        if (_p2pState.value is P2PState.Running) return
        val user = get<UserRespository>(UserRespository::class.java)
        val userInfo = user.currentUser.value!!
        println("MainViewModel: Starting P2P services...")

        viewModelScope.launch {
            serverManager.start(
                port = 0,
                userUid = userInfo.uid,
                userName = userInfo.name,
                chatViewModel = chatViewModel,
                peerDiscoveryViewModel
            )
        }

        _p2pState.value = P2PState.Running(userInfo.uid, userInfo.name)
    }

    fun stopP2PServices() {
        println("MainViewModel: Stopping P2P services...")
        serverManager.close()
        peerDiscoveryViewModel.stopDiscovery()
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

@Composable
fun App(
    onLoginSuccess: () -> Unit
) {
    createKtorClient()
    val isMobile = getPlatformType() == PlatformType.ANDROID
    val database: DatabaseMng = koinInject()

    AppTheme {
        Column(
            modifier =
                if (isMobile) {
                    Modifier
                        .background(Color.Black)
                        .displayCutoutPadding()
                        .fillMaxSize()
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .safeContentPadding()
                        .fillMaxSize()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (database.getSize() == 0) {
                Navigator(LoginFields(onLoginSuccess)){ nav ->
                    val appNavigator = remember{ AppNavigator(nav, null) }
                    CompositionLocalProvider(LocalAppNavigator provides appNavigator) {
                        CurrentScreen()
                    }
                }
            } else {
                Navigator(MainScreen()){ nav ->
                    val appNavigator = remember{ AppNavigator(nav, null) }
                    CompositionLocalProvider(LocalAppNavigator provides appNavigator) {
                        CurrentScreen()
                    }
                }
            }
        }
    }
}