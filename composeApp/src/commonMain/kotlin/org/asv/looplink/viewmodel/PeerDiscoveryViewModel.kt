package org.asv.looplink.viewmodel

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.discovery.ServiceInfo
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewModelScope

sealed class ConnectionStatus {
    object Idle : ConnectionStatus()
    object Connecting : ConnectionStatus()
    data class Connected(val session: DefaultClientWebSocketSession) : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

class PeerDiscoveryViewModel(
    private val serviceDiscovery: LANServiceDiscovery,
    private val chatViewModel: ChatViewModel,
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _discoveredServices = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val discoveredServices = _discoveredServices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus = _connectionStatus.asStateFlow()

    val _activeSessions = MutableStateFlow<Map<Int, DefaultWebSocketSession>>(emptyMap())
    val activeSessions = _activeSessions.asStateFlow()

    private val JMDNS_SERVICE_TYPE = "_looplink._tcp.local." // Keep for reference if needed
    private val NSD_SERVICE_TYPE = "_looplink._tcp." // Platform-agnostic type

    // Use this as the primary service type for discovery requests
    val currentDiscoveryServiceType = NSD_SERVICE_TYPE

    fun startDiscovery() {
        if (_isDiscovering.value) return
        println("PDVM started to discover for type: $currentDiscoveryServiceType")
        _isDiscovering.value = true
        _discoveredServices.value = emptyList()

        serviceDiscovery.discoverServices(currentDiscoveryServiceType).onEach { services ->
            println("PDVM: Discovered services: ${services.map { it.instanceName }}")
            _discoveredServices.value = services
        }.catch { e ->
            println("PDVM: Error discovering services: ${e.message}")
            _isDiscovering.value = false
        }.launchIn(viewModelScope)
    }

    fun stopDiscovery() {
        println("PDVM: Stopping discovery")
        serviceDiscovery.stopDiscovery(currentDiscoveryServiceType)
        _isDiscovering.value = false
    }

    fun clear() {
        println("PDVM: Clearing")
        stopDiscovery()
        viewModelScope.cancel()
    }

    fun connectToService(
        service: ServiceInfo,
        localUserName: String,
        localUserUid: String
    ) {
        val host = service.hostAddress
        val peerUid = service.attributes["uid"] ?: return
        val peerName = service.attributes["name"] ?: "Unknown"

        println("PDVM: Attempting to connect to: $peerName at $host:${service.port}")

        val uids = listOf(localUserUid, peerUid).sorted()
        val roomId = (uids[0] + uids[1]).hashCode()

        val newRoom = RoomItem(roomId, label = peerName, members = listOf(localUserUid, peerUid))
        chatViewModel.addRoom(newRoom)

        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting
            try {
                val encodedUID = URLEncoder.encode(localUserUid, StandardCharsets.UTF_8.toString())
                val encodedName =
                    URLEncoder.encode(localUserName, StandardCharsets.UTF_8.toString())

                val client = createKtorClient()
                val session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = host,
                    port = service.port,
                    path = "/looplink/sync/$roomId?peerUid=$encodedUID&peerName=$encodedName"
                )
                _connectionStatus.value = ConnectionStatus.Connected(session)
                _activeSessions.update { it + (roomId to session) }
                println("PDVM: WebSocket connection established and session stored for room $roomId.")
            } catch (e: Exception) {
                _connectionStatus.value = ConnectionStatus.Error("Failed to connect: ${e.message}")
                println("PDVM: WebSocket connection failed: ${e.message}")
            }
        }
    }

    fun addConnection(roomId: Int, session: DefaultWebSocketSession) {
        _activeSessions.update { it + (roomId to session) }
        println("PDVM: Session added room $roomId to active sessions.")
    }

    fun removeConnection(roomId: Int) {
        _activeSessions.update {
            it - roomId
        }
        println("PDVM: Session removed for room $roomId.")
    }
}