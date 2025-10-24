package org.asv.looplink.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.asv.looplink.DatabaseMng
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.network.ServerManager
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.webDriver.cuimsAPI

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
    val userRepository: UserRespository,
    private val serverManager: ServerManager
) : ViewModel() {
    private val _p2pState = MutableStateFlow<P2PState>(P2PState.Stopped)
    val p2pState = _p2pState.asStateFlow()

    init {
        println("MainViewModel: Initializing...")
        // Check if user is already logged in
        val userData = database.getSize()
        if (userData > 0) {
            userRepository.loadUser()
            if (_p2pState.value is P2PState.Stopped) startP2PServices()
        }
    }

    fun startP2PServices() {
        if (_p2pState.value is P2PState.Running) return
        val userInfo = userRepository.currentUser.value ?: return
        println("MainViewModel: Starting P2P services...")
        serverManager.start(userInfo.uid, userInfo.name)
        _p2pState.value = P2PState.Running(userInfo.uid, userInfo.name)
    }

    fun stopP2PServices() {
        println("MainViewModel: Stopping P2P services...")
        if(_p2pState.value is P2PState.Stopped) return
        serverManager.stop()
        lanServiceDiscovery.stopDiscovery()
        peerDiscoveryViewModel.stopDiscovery()
        _p2pState.value = P2PState.Stopped
    }

    fun logoutUser(){
        stopP2PServices()
        database.deleteUser()
        userRepository.logout()
    }

    override fun onCleared() {
        println("MainViewModel: Clearing...")
        stopP2PServices()
        serverManager.close()
        lanServiceDiscovery.close()
        cuimsAPI.destroySession()
        super.onCleared()
    }
}