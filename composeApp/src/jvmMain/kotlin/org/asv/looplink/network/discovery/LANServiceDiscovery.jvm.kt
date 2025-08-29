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
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import javax.jmdns.ServiceInfo as JmDnsServiceInfo

actual class LANServiceDiscovery actual constructor(){

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var jmDnsInstance: JmDNS? = null
    private val registeredService = mutableListOf<JmDnsServiceInfo>()
    private val discoveredServices = mutableMapOf<String, ServiceListener>()

    init {
        coroutineScope.launch {
            try {
                jmDnsInstance = JmDNS.create(Inet4Address.getLocalHost())
                println("JmDNS initialized")
            } catch (e: Exception){
                println("Error in JmDNS: ${e.message}")
            }
        }
    }

    actual fun discoverServices(serviceType: String): Flow<List<ServiceInfo>> {
        val jmdns = jmDnsInstance ?: return MutableStateFlow(emptyList())

        discoveredServices[serviceType]?.let{
            listener ->
            println("Removing exiting listner for $serviceType")
            jmdns.removeServiceListener(serviceType, listener)
            discoveredServices.remove(serviceType)
        }

        val currentServicesFlow = MutableStateFlow<Map<String, ServiceInfo>>(emptyMap())

        val listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                println("JmDNS: Service Added: ${event.name}, type: ${event.type}")
                jmdns.requestServiceInfo(event.type, event.name, 1000)
            }

            override fun serviceRemoved(event: ServiceEvent?) {
                val currentMap = currentServicesFlow.value.toMutableMap()
                val serviceKey = "${event?.type}:${event?.name}"
                currentMap.remove(serviceKey)
                currentServicesFlow.value = currentMap

            }

            override fun serviceResolved(event: ServiceEvent) {
                val jmdnsInfo = event.info

                if(jmdnsInfo.inet4Addresses.isNotEmpty() && jmdnsInfo.port > 0){
                    val serviceInfo = ServiceInfo(
                        instanceName = jmdnsInfo.name,
                        serviceName = jmdnsInfo.type,
                        port = jmdnsInfo.port,
                        hostAddress = jmdnsInfo.inet4Addresses[0].hostAddress ?: "Unknown",
                        attributes = jmdnsInfo.propertyNames.asSequence()
                            .mapNotNull { name ->
                            jmdnsInfo.getPropertyString(name)?.let{name to it}
                        }.toMap()
                    )

                    if(serviceInfo.hostAddress.isNotBlank()){
                        val currentMap = currentServicesFlow.value.toMutableMap()
                        val serviceKey = "${serviceInfo.serviceName}:${serviceInfo.instanceName}"
                        currentMap[serviceKey] = serviceInfo
                        currentServicesFlow.value = currentMap
                    } else {
                        println("JmDNS: Resolved service ${serviceInfo.instanceName} has invalid address: ${serviceInfo.hostAddress}")
                    }
                } else {
                    println("JmDNS: Resolved service ${jmdnsInfo.name} has no IPv4 address or invalid port.")
                }
            }
        }

        jmdns.addServiceListener(serviceType, listener)
        discoveredServices[serviceType] = listener
        println("JmDNS: Adding service listener for type: $serviceType")

        return callbackFlow {
            val flowListener = listener
            val job = launch {
                currentServicesFlow.collect { map ->
                    trySend(map.values.toList())
                }
            }

            awaitClose {
                jmdns.removeServiceListener(serviceType, flowListener)
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
        val jmdns = jmDnsInstance ?: run{
            println("JmDNS not initialied")
            return
        }

        coroutineScope.launch{
            try {
                val verifedServiceType = if(serviceType.endsWith(".")) serviceType else "$serviceType."

                val ServiceInfo = JmDnsServiceInfo.create(
                    verifedServiceType,
                    instanceName,
                    port,
                    0,
                    0,
                    attributes
                )
                jmdns.registerService(ServiceInfo)
                registeredService.add(ServiceInfo)
                println("Service registered: $ServiceInfo on port $port")
            } catch (e: Exception){
                println("Error in JmDNS: ${e.message}")
            }
        }.join()
    }

    actual suspend fun unregistedService() {
        val jmDns = jmDnsInstance ?: return
        if (registeredService.isEmpty()) {
            println("JmDNS: No services to unregister.")
            return
        }
        coroutineScope.launch{
            if(registeredService.isNotEmpty()){
                registeredService.forEach { serviceInfo ->
                    try {
                        jmDns.unregisterService(serviceInfo)
                    } catch (e: Exception){
                        println("Error in JmDNS: ${e.message} for ${serviceInfo.name}")
                    }
                }
                registeredService.clear()
            }
        }.join()
    }

    actual fun stopDiscovery() {
        coroutineScope.launch {
            jmDnsInstance?.let{
                try {
                    it.close()
                } catch (e: Exception){
                    println("Error closing JmDNS: ${e.message}")
                }
                jmDnsInstance = null
            }
        }
    }

    fun close(){
        coroutineScope.launch {
            unregistedService()
            coroutineScope.cancel()
            jmDnsInstance?.close()
            jmDnsInstance = null
        }
    }

    actual fun stopDiscovery(serviceType: String?) {
        if(serviceType != null){
            discoveredServices.remove(serviceType)?.let{
                listener ->
                println("JmDNS: Removing listener for $serviceType")
                jmDnsInstance?.removeServiceListener(serviceType, listener)
            }
        } else {
            println("JmDNS: Removing all active service listeners")
            discoveredServices.forEach{ (type, listener) ->
                jmDnsInstance?.removeServiceListener(type, listener)
            }
            discoveredServices.clear()
        }
    }
}