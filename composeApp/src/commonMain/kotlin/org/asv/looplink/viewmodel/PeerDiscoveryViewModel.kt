package org.asv.looplink.viewmodel

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
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.discovery.ServiceInfo

class PeerDiscoveryViewModel(
    private val serviceDiscovery: LANServiceDiscovery,
    private val externalScope: CoroutineScope? = null
) {
    private val viewModelScope = externalScope ?: CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _discoveredServices = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val discoveredServices = _discoveredServices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val JMDNS_SERVICE_TYPE = "_looplink._tcp.local." // Keep for reference if needed
    private val NSD_SERVICE_TYPE = "_looplink._tcp" // Platform-agnostic type

    // Use this as the primary service type for discovery requests
    val currentDiscoveryServiceType = NSD_SERVICE_TYPE 

    fun startDiscovery() {
        if (_isDiscovering.value) return
        println("PDVM stated to discover for type: $currentDiscoveryServiceType")
        _isDiscovering.value = true
        _discoveredServices.value = emptyList()

        serviceDiscovery.discoverServices(currentDiscoveryServiceType).onEach { services ->
            println("PDVM: Discovered services: ${services.map { it.instanceName }}")
            _discoveredServices.value = services
        }.catch{
            e -> println("PDVM: Error discovering services: ${e.message}")
            _isDiscovering.value = false
        }.launchIn(viewModelScope)
    }

    fun stopDiscovery(){
        println("PDVM: Stopping discovery")
        serviceDiscovery.stopDiscovery(currentDiscoveryServiceType)
        _isDiscovering.value = false
    }

    fun clear(){
        println("PDVM: Clearing")
        stopDiscovery()
        if(externalScope == null) viewModelScope.cancel()
    }

    fun connectToService(service: ServiceInfo) {
        val host = service.hostAddress
        println("PDVM: Attempting to connect to: ${service.serviceName} at $host:${service.port}")
        // TODO: Implement Ktor client connection logic here
        // This will involve creating/using a Ktor HttpClient,
        // selecting a protocol (e.g., WebSockets), and initiating the connection.
    }
}