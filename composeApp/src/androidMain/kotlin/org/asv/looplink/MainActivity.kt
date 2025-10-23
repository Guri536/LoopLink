package org.asv.looplink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.asv.looplink.components.loadUserInfo
import org.asv.looplink.components.userInfo
import org.asv.looplink.network.AndroidKtorServer
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val onLoginSuccess = {
                viewModel.startP2PServices()
            }
            App(
                onLoginSuccess
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
