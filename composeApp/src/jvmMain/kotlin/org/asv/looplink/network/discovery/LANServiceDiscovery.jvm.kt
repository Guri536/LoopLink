package org.asv.looplink.network.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// import kotlinx.coroutines.withContext // Not used currently
import java.net.Inet4Address
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.collections.emptyList
import javax.jmdns.ServiceInfo as JmDnsServiceInfo

actual class LANServiceDiscovery actual constructor(){

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var jmDnsInstance: JmDNS? = null
    private val registeredServiceInfoList = mutableListOf<JmDnsServiceInfo>() // Renamed for clarity
    private val activeDiscoveryListeners = mutableMapOf<String, ServiceListener>() // Renamed for clarity

    init {
        coroutineScope.launch {
            try {
                // Prefer binding to a specific interface if possible, or let JmDNS pick
                // val hostAddress = InetAddress.getLocalHost() // Can sometimes pick loopback
                // Forcing a specific non-loopback address might be more robust if issues arise
                jmDnsInstance = JmDNS.create(null, null) // Bind to all addresses, assign a default name
                println("JmDNS initialized")
            } catch (e: Exception){
                println("Error initializing JmDNS: ${e.message}")
                e.printStackTrace() // For more detailed error
            }
        }
    }

    // Helper to ensure service type is JmDNS compatible (e.g., _mytype._tcp.local.)
    private fun adaptServiceTypeForJmDNS(serviceType: String): String {
        val type = serviceType.removeSuffix(".") // Sanitize
        // Check if it's in the common "_service._protocol" format
        if (type.startsWith("_") && (type.endsWith("._tcp") || type.endsWith("._udp"))) {
            val parts = type.split('.')
            // If it's already "_service._protocol.local"
            if (parts.size == 3 && parts[2].equals("local", ignoreCase = true)) {
                return "$type." // Ensure trailing dot
            }
            // If it's "_service._protocol", convert to "_service._protocol.local."
            if (parts.size == 2) {
                return "$type.local."
            }
        }
        // If not matching the common pattern or already qualified, ensure it ends with a dot.
        return if (type.endsWith(".")) type else "$type."
    }


    actual fun discoverServices(serviceType: String): Flow<List<ServiceInfo>> {
        val jmdnsCompatibleServiceType = adaptServiceTypeForJmDNS(serviceType)
        val jmdns = jmDnsInstance ?: return MutableStateFlow(emptyList<ServiceInfo>())
            .also {
            println("JmDNS not initialized, cannot discover services for $jmdnsCompatibleServiceType")
        }

        // Stop and remove previous listener for this specific type if it exists
        activeDiscoveryListeners[jmdnsCompatibleServiceType]?.let { existingListener ->
            println("JmDNS: Removing existing listener for $jmdnsCompatibleServiceType")
            try {
                jmdns.removeServiceListener(jmdnsCompatibleServiceType, existingListener)
            } catch (e: Exception) {
                println("JmDNS: Error removing existing listener for $jmdnsCompatibleServiceType: ${e.message}")
            }
            activeDiscoveryListeners.remove(jmdnsCompatibleServiceType)
        }

        val currentServicesFlow = MutableStateFlow<Map<String, ServiceInfo>>(emptyMap())

        val listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                // event.type should already be JmDNS compatible (e.g., _looplink._tcp.local.)
                println("JmDNS: Service Added: ${event.name}, type: ${event.type}")
                // Request resolution for the specific service
                jmdns.requestServiceInfo(event.type, event.name, 1000) // 1 sec timeout for resolve
            }

            override fun serviceRemoved(event: ServiceEvent) {
                println("JmDNS: Service Removed: ${event.name}, type: ${event.type}")
                val currentMap = currentServicesFlow.value.toMutableMap()
                // Construct key based on how it's added in serviceResolved
                val serviceKey = "${event.type}${event.name}"
                if (currentMap.remove(serviceKey) != null) {
                    currentServicesFlow.value = currentMap
                }
            }

            override fun serviceResolved(event: ServiceEvent) {
                val jmdnsInfo = event.info ?: return Unit.also {
                    println("JmDNS: Service Resolved but event info is null for ${event.name}")
                }
                println("JmDNS: Service Resolved: ${jmdnsInfo.niceTextString}")

                // Prefer IPv4 addresses for LAN, ensure host address is not empty or loopback for remote
                val suitableHostAddress = jmdnsInfo.inet4Addresses.firstOrNull { !it.isLoopbackAddress }?.hostAddress
                    ?: jmdnsInfo.inetAddresses.firstOrNull { !it.isLoopbackAddress }?.hostAddress


                if (suitableHostAddress != null && jmdnsInfo.port > 0) {
                    val serviceAttributes = jmdnsInfo.propertyNames.asSequence()
                        .mapNotNull { name -> jmdnsInfo.getPropertyString(name)?.let { name to it } }
                        .toMap()

                    val serviceInfo = ServiceInfo(
                        instanceName = jmdnsInfo.name, // Usually the instance name like "MyPC"
                        serviceName = jmdnsInfo.type, // Fully qualified like _looplink._tcp.local.
                        port = jmdnsInfo.port,
                        hostAddress = suitableHostAddress,
//                        hostAddresses = jmdnsInfo.inetAddresses.mapNotNull { it.hostAddress },
                        attributes = serviceAttributes
                    )

                    val currentMap = currentServicesFlow.value.toMutableMap()
                    // Use a consistent key for adding/removing. Using type+name from event.
                    val serviceKey = "${event.type}${event.name}"
                    currentMap[serviceKey] = serviceInfo
                    currentServicesFlow.value = currentMap
                    println("JmDNS: Updated service map with ${serviceInfo.instanceName}")

                } else {
                    println("JmDNS: Resolved service ${jmdnsInfo.name} has no suitable address or invalid port. Addresses: ${jmdnsInfo.hostAddresses.joinToString()}, Port: ${jmdnsInfo.port}")
                }
            }
        }

        try {
            jmdns.addServiceListener(jmdnsCompatibleServiceType, listener)
            activeDiscoveryListeners[jmdnsCompatibleServiceType] = listener
            println("JmDNS: Added service listener for type: $jmdnsCompatibleServiceType")
        } catch (e: Exception) {
            println("JmDNS: Error adding service listener for $jmdnsCompatibleServiceType: ${e.message}")
            return MutableStateFlow(emptyList())
        }


        return callbackFlow {
            val flowListener = listener // Capture the listener instance for awaitClose
            val job = launch {
                currentServicesFlow.collect { map ->
                    trySend(map.values.toList().distinctBy { it.instanceName + it.hostAddress + it.port }) // Ensure uniqueness
                }
            }
            awaitClose {
                println("JmDNS: Closing discovery flow for $jmdnsCompatibleServiceType. Removing listener.")
                try {
                    jmdns.removeServiceListener(jmdnsCompatibleServiceType, flowListener)
                } catch (e: Exception) {
                     println("JmDNS: Error removing listener in awaitClose for $jmdnsCompatibleServiceType: ${e.message}")
                }
                activeDiscoveryListeners.remove(jmdnsCompatibleServiceType)
                job.cancel()
            }
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    actual suspend fun registerService(
        instanceName: String,
        serviceType: String,
        port: Int,
        attributes: Map<String, String>
    ) {
        val jmdnsCompatibleServiceType = adaptServiceTypeForJmDNS(serviceType)
        val jmdns = jmDnsInstance ?: return Unit.also {
            println("JmDNS not initialized, cannot register service $instanceName")
        }

        // Run on IO dispatcher as JmDNS operations can block
        coroutineScope.launch {
            try {
                // Remove existing service with the same name before registering new one to avoid conflicts
                registeredServiceInfoList.find { it.name == instanceName && it.type == jmdnsCompatibleServiceType }?.let {
                    println("JmDNS: Unregistering existing service with same name/type before re-registering: $instanceName")
                    jmdns.unregisterService(it)
                    registeredServiceInfoList.remove(it)
                }

                val serviceInfoToRegister = JmDnsServiceInfo.create(
                    jmdnsCompatibleServiceType,
                    instanceName,
                    port,
                    0, // weight
                    0, // priority
                    attributes
                )
                jmdns.registerService(serviceInfoToRegister)
                registeredServiceInfoList.add(serviceInfoToRegister)
                println("JmDNS: Service registered: ${serviceInfoToRegister.niceTextString} on port $port")
            } catch (e: Exception){
                println("JmDNS: Error registering service '$instanceName': ${e.message}")
                e.printStackTrace()
            }
        }.join() // Ensure completion if called from a suspending context that expects it
    }

    actual suspend fun unregistedService() { // Should be unregisterService (typo in expect)
        val jmdns = jmDnsInstance ?: return
        if (registeredServiceInfoList.isEmpty()) {
            println("JmDNS: No services to unregister.")
            return
        }
        coroutineScope.launch {
            println("JmDNS: Unregistering all services...")
            // Iterate over a copy to avoid ConcurrentModificationException if unregisterService modifies the list
            ArrayList(registeredServiceInfoList).forEach { serviceInfo ->
                try {
                    jmdns.unregisterService(serviceInfo)
                    println("JmDNS: Unregistered ${serviceInfo.name}")
                } catch (e: Exception){
                    println("JmDNS: Error unregistering service ${serviceInfo.name}: ${e.message}")
                }
            }
            registeredServiceInfoList.clear()
            println("JmDNS: All services unregistered.")
        }.join()
    }
    actual fun stopDiscovery(serviceType: String?) {
        val jmdns = jmDnsInstance ?: return

        if (serviceType != null) {
            val jmdnsCompatibleServiceType = adaptServiceTypeForJmDNS(serviceType)
            activeDiscoveryListeners.remove(jmdnsCompatibleServiceType)?.let { listener ->
                println("JmDNS: Removing listener for $jmdnsCompatibleServiceType")
                try {
                    jmdns.removeServiceListener(jmdnsCompatibleServiceType, listener)
                } catch (e: Exception) {
                    println("JmDNS: Error removing listener for $jmdnsCompatibleServiceType: ${e.message}")
                }
            }
        } else {
            println("JmDNS: Removing all active service listeners")
            // Iterate over a copy of keys to avoid ConcurrentModificationException
            ArrayList(activeDiscoveryListeners.keys).forEach { type ->
                activeDiscoveryListeners.remove(type)?.let { listener ->
                    try {
                        jmdns.removeServiceListener(type, listener) // type is already jmdnsCompatible here
                    } catch (e: Exception) {
                        println("JmDNS: Error removing listener for $type: ${e.message}")
                    }
                }
            }
            activeDiscoveryListeners.clear()
        }
    }


    // This is the global close for the entire LANServiceDiscovery instance
    fun close() {
        println("JmDNS: Closing LANServiceDiscovery. Unregistering services and stopping all discoveries.")
        coroutineScope.launch {
            unregistedService() // Call the suspend version
            stopDiscovery(null) // Stop all discovery listeners

            jmDnsInstance?.let {
                try {
                    println("JmDNS: Closing JmDNS instance.")
                    it.close()
                } catch (e: Exception) {
                    println("JmDNS: Error closing JmDNS instance: ${e.message}")
                    e.printStackTrace()
                }
                jmDnsInstance = null
            }
            coroutineScope.cancel() // Cancel the scope itself
            println("JmDNS: LANServiceDiscovery closed.")
        }
    }

    // Deprecated stopDiscovery, use close() for full cleanup or stopDiscovery(serviceType) for specific
    @Deprecated("Use close() for full cleanup or stopDiscovery(specificServiceType)", ReplaceWith("this.close() or this.stopDiscovery(null)"))
    actual fun stopDiscovery() { // This was the old global stop, redirect or clarify its use
         println("JmDNS: stopDiscovery() called - stopping all listeners and unregistering. Consider using close() or stopDiscovery(type).")
         stopDiscovery(null) // Stop all listeners
    }
}