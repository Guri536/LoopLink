package org.asv.looplink.network.discovery

import kotlinx.coroutines.CompletableDeferred
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
import kotlinx.io.IOException
import java.net.Inet4Address
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.collections.emptyList
import javax.jmdns.ServiceInfo as JmDnsServiceInfo

actual class LANServiceDiscovery actual constructor(){

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val initialization = CompletableDeferred<JmDNS>()
    private var jmDnsInstance: JmDNS? = null
    private val registeredServiceInfoList = mutableListOf<JmDnsServiceInfo>() // Renamed for clarity
    private val activeDiscoveryListeners = mutableMapOf<String, ServiceListener>() // Renamed for clarity

    fun initialize(){
        if(initialization.isCompleted) return
        coroutineScope.launch {
            try {
                jmDnsInstance = JmDNS.create(null, null)
                initialization.complete(jmDnsInstance?:throw(NullPointerException("JmDNS instance is null")))
                println("JmDNS initialized successfully.")
            } catch (e: IOException) {
                println("Error initializing JmDNS: ${e.message}")
                initialization.completeExceptionally(e)
            }
        }
    }

//    init {
//        coroutineScope.launch {
//            try {
//                jmDnsInstance = JmDNS.create(null, null) // Bind to all addresses, assign a default name
//                println("JmDNS initialized")
//            } catch (e: Exception){
//                println("Error initializing JmDNS: ${e.message}")
//                e.printStackTrace() // For more detailed error
//            }
//        }
//    }

    private fun adaptServiceTypeForJmDNS(serviceType: String): String {
        val type = serviceType.removeSuffix(".")
        return if (type.endsWith("._tcp") || type.endsWith("._udp")) {
            "$type.local."
        } else {
            "$type."
        }
    }

    actual fun discoverServices(serviceType: String): Flow<List<ServiceInfo>> {
        val jmdnsCompatibleServiceType = adaptServiceTypeForJmDNS(serviceType)
        println("Trying to discover services for $jmdnsCompatibleServiceType")
//        val jmdns = jmDnsInstance ?: return MutableStateFlow(emptyList<ServiceInfo>())
//            .also {
//            println("JmDNS not initialized, cannot discover services for $jmdnsCompatibleServiceType")
//        }

        activeDiscoveryListeners[jmdnsCompatibleServiceType]?.let { existingListener ->
            println("JmDNS: Removing existing listener for $jmdnsCompatibleServiceType")
            try {
                jmDnsInstance?.removeServiceListener(jmdnsCompatibleServiceType, existingListener)
            } catch (e: Exception) {
                println("JmDNS: Error removing existing listener for $jmdnsCompatibleServiceType: ${e.message}")
            }
            activeDiscoveryListeners.remove(jmdnsCompatibleServiceType)
        }

        val currentServicesFlow = MutableStateFlow<Map<String, ServiceInfo>>(emptyMap())

        val listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                println("JmDNS: Service Added: ${event.name}, type: ${event.type}")
                jmDnsInstance?.requestServiceInfo(event.type, event.name, 1000) // 1 sec timeout for resolve
            }

            override fun serviceRemoved(event: ServiceEvent) {
                println("JmDNS: Service Removed: ${event.name}, type: ${event.type}")
                val currentMap = currentServicesFlow.value.toMutableMap()
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

                val suitableHostAddress = jmdnsInfo.inet4Addresses.firstOrNull { !it.isLoopbackAddress }?.hostAddress
                    ?: jmdnsInfo.inetAddresses.firstOrNull { !it.isLoopbackAddress }?.hostAddress


                if (suitableHostAddress != null && jmdnsInfo.port > 0) {
                    val serviceAttributes = jmdnsInfo.propertyNames.asSequence()
                        .mapNotNull { name -> jmdnsInfo.getPropertyString(name)?.let { name to it } }
                        .toMap()

                    val serviceInfo = ServiceInfo(
                        instanceName = jmdnsInfo.name,
                        serviceName = jmdnsInfo.type.removeSuffix("."),
                        port = jmdnsInfo.port,
                        hostAddress = suitableHostAddress,
                        attributes = serviceAttributes
                    )

                    val currentMap = currentServicesFlow.value.toMutableMap()
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
            jmDnsInstance?.addServiceListener(jmdnsCompatibleServiceType, listener)
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
                    jmDnsInstance?.removeServiceListener(jmdnsCompatibleServiceType, flowListener)
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

        coroutineScope.launch {
            try {
                registeredServiceInfoList.find { it.name == instanceName && it.type == jmdnsCompatibleServiceType }?.let {
                    println("JmDNS: Unregistering existing service with same name/type before re-registering: $instanceName")
                    jmDnsInstance?.unregisterService(it)
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
                jmDnsInstance?.registerService(serviceInfoToRegister)
                registeredServiceInfoList.add(serviceInfoToRegister)
                println("JmDNS: Service registered: ${serviceInfoToRegister.niceTextString} on port $port")
            } catch (e: Exception){
                println("JmDNS: Error registering service '$instanceName': ${e.message}")
                e.printStackTrace()
            }
        }.join()
    }

    actual suspend fun unregisterService() { // Should be unregisterService (typo in expect)
//        val jmdns = jmDnsInstance ?: return
        if (registeredServiceInfoList.isEmpty()) {
            println("JmDNS: No services to unregister.")
            return
        }
        coroutineScope.launch {
            println("JmDNS: Unregistering all services...")
            ArrayList(registeredServiceInfoList).forEach { serviceInfo ->
                try {
                    jmDnsInstance?.unregisterService(serviceInfo)
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
//        val jmdns = jmDnsInstance ?: return

        if (serviceType != null) {
            val jmdnsCompatibleServiceType = adaptServiceTypeForJmDNS(serviceType)
            activeDiscoveryListeners.remove(jmdnsCompatibleServiceType)?.let { listener ->
                println("JmDNS: Removing listener for $jmdnsCompatibleServiceType")
                try {
                    jmDnsInstance?.removeServiceListener(jmdnsCompatibleServiceType, listener)
                } catch (e: Exception) {
                    println("JmDNS: Error removing listener for $jmdnsCompatibleServiceType: ${e.message}")
                }
            }
        } else {
            println("JmDNS: Removing all active service listeners")
            ArrayList(activeDiscoveryListeners.keys).forEach { type ->
                activeDiscoveryListeners.remove(type)?.let { listener ->
                    try {
                        jmDnsInstance?.removeServiceListener(type, listener) // type is already jmdnsCompatible here
                    } catch (e: Exception) {
                        println("JmDNS: Error removing listener for $type: ${e.message}")
                    }
                }
            }
            activeDiscoveryListeners.clear()
        }
    }

    fun close() {
        println("JmDNS: Closing LANServiceDiscovery. Unregistering services and stopping all discoveries.")
        coroutineScope.launch {
            unregisterService() // Call the suspend version
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

    @Deprecated("Use close() for full cleanup or stopDiscovery(specificServiceType)", ReplaceWith("this.close() or this.stopDiscovery(null)"))
    actual fun stopDiscovery() { // This was the old global stop, redirect or clarify its use
         println("JmDNS: stopDiscovery() called - stopping all listeners and unregistering. Consider using close() or stopDiscovery(type).")
         stopDiscovery(null) // Stop all listeners
    }
}