package org.asv.looplink.viewmodel

import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpMethod
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
import kotlinx.coroutines.launch
import org.asv.looplink.components.chat.User
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.DIRECTORIES
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.network.ConnectionManager
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.discovery.ServiceInfo
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.ui.RoomItem
import org.koin.java.KoinJavaComponent.get
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PeerDiscoveryViewModel(
    private val serviceDiscovery: LANServiceDiscovery,
    private val chatViewModel: ChatViewModel,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val connectionManager: ConnectionManager
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _discoveredServices = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val discoveredServices = _discoveredServices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val JMDNS_SERVICE_TYPE = "_looplink._tcp.local." // Keep for reference if needed
    private val NSD_SERVICE_TYPE = "_looplink._tcp." // Platform-agnostic type

    // Use this as the primary service type for discovery requests
    val currentDiscoveryServiceType = NSD_SERVICE_TYPE
    private val fileRepository: FileRepository = get(FileRepository::class.java)

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
            chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Connecting)
            try {
                val encodedUID = URLEncoder.encode(localUserUid, StandardCharsets.UTF_8.toString())
                val encodedName =
                    URLEncoder.encode(localUserName, StandardCharsets.UTF_8.toString())

                val localUserPort = userRepository.currentUserPort.value
                if (localUserPort == 0) {
                    println("PDVM: Error - Local server port is 0. Cannot connect.")
                    chatViewModel.updateRoomConnection(
                        roomId,
                        ConnectionStatus.Error("Local server not running")
                    )
                    return@launch
                }
                val encodedPort = localUserPort.toString()

                val client = createKtorClient()
                val session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = host,
                    port = service.port,
                    path = "/looplink/initiate/$roomId?peerUid=$encodedUID&peerName=$encodedName&peerPort=$encodedPort"
                )
                chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Connected)
                connectionManager.addPeer(host)
                chatRepository.addAndListenToClientSession(roomId, session, host)
                println("PDVM: WebSocket connection established and session stored for room $roomId.")

                userRepository.addUserToCache(
                    User(
                        peerUid,
                        peerName,
                        null,
                        host,
                        service.port.toString()
                    )
                )
            } catch (e: Exception) {
                chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Error("Error"))
                println("PDVM: WebSocket connection failed: ${e.message}")
            }
        }
    }
}