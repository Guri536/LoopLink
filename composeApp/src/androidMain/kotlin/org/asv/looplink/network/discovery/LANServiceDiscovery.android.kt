package org.asv.looplink.network.discovery

import kotlinx.coroutines.flow.Flow
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class LANServiceDiscovery actual constructor() {
    private var context: Context? = null
    constructor(context: Context): this(){
        this.context = context
    }

    private val nsdManager: NsdManager? by lazy{
        context?.getSystemService(Context.NSD_SERVICE) as? NsdManager
    }

    private var activeDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var activeServiceType: String? = null
    private var activeRegistrationListener: NsdManager.RegistrationListener? = null

    actual fun discoverServices(serviceType: String): Flow<List<ServiceInfo>> {
        val manager = nsdManager ?: return MutableStateFlow(emptyList())

        stopPreviousDiscovery()
        this.activeServiceType = serviceType

        return callbackFlow<List<ServiceInfo>> {
            val discoveredServiesMap = mutableMapOf<String, ServiceInfo>()

            val discoveryListener = object : NsdManager.DiscoveryListener{
                override fun onDiscoveryStarted(p0: String?) {
                    println("Started NsD discovery for $p0")
                }

                override fun onServiceFound(p0: NsdServiceInfo?) {
                    manager.resolveService(p0,
                        object : NsdManager.ResolveListener{
                            override fun onResolveFailed(
                                p0: NsdServiceInfo?,
                                p1: Int
                            ) {
                                println("Couldn't resolve service ${p0?.serviceName}")
                            }

                            override fun onServiceResolved(p0: NsdServiceInfo?) {
                                val attributes = mutableMapOf<String, String>()

                                p0?.attributes?.forEach { attribute ->
                                    attributes[attribute.key] = attribute.value.toString(Charsets.UTF_8)
                                }

                                val service = ServiceInfo(
                                    instanceName = p0?.serviceName ?: "Unknown",
                                    serviceName = p0?.serviceName ?: "Unknown",
                                    port = p0?.port ?: 0,
                                    hostAddress = p0?.host?.hostAddress ?: "Unknown",
                                    attributes = attributes
                                )

                                if(service.hostAddress.isNotBlank() && service.port > 0){
                                    synchronized(discoveredServiesMap){
                                        discoveredServiesMap[service.serviceName] = service
                                        trySend(discoveredServiesMap.values.toList())
                                    }
                                }
                            }

                        })
                }

                override fun onServiceLost(p0: NsdServiceInfo?) {
                    println("NSD: Service List ${p0?.serviceName}")
                    synchronized(discoveredServiesMap){
                        discoveredServiesMap.remove(p0?.serviceName)
                        trySend(discoveredServiesMap.values.toList())
                    }
                }

                override fun onDiscoveryStopped(p0: String?) {
                    println("NSD: Discovery stopped for $p0")
                    activeDiscoveryListener = null
                }


                override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
                    println("NSD: Start Discovery Failed for $p0 on error $p1")
                    close(RuntimeException("Discovery failed due to $p1"))
                    activeDiscoveryListener = null
                }

                override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
                    println("NSD: Stop Discovery Failed for $p0 on error $p1")
                    activeDiscoveryListener = null
                }
            }

            activeDiscoveryListener = discoveryListener
            manager.discoverServices(serviceType,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
            awaitClose {
                println("NSD: Closing discovery for $serviceType")
                stopPreviousDiscovery()
            }
        }.stateIn(CoroutineScope(Dispatchers.IO),
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    private fun stopPreviousDiscovery() {
        activeDiscoveryListener?.let{
            listener ->
            nsdManager?.stopServiceDiscovery(listener)
            activeDiscoveryListener = null
            println("NSD: Previous Discovery Stopped")
        }
        activeServiceType = null
    }

    actual suspend fun registerService(
        instanceName: String,
        serviceType: String,
        port: Int,
        attributes: Map<String, String>
    ) = suspendCancellableCoroutine {
        continuation ->
        val manager = nsdManager ?: run{
            continuation.resumeWithException(
                RuntimeException("NSDManager not available")
            )
            return@suspendCancellableCoroutine
        }

        unregisterServiceInternal()

        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = instanceName
            this.serviceType = serviceType
            this.port = port
            attributes.forEach { attribute ->
                setAttribute(attribute.key, attribute.value)
            }
        }

        val registrationListener = object : NsdManager.RegistrationListener{
            override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
                println("NSD: Registration failed for ${p0?.serviceName}: error $p1")
                activeRegistrationListener = null
                if(continuation.isActive){
                    continuation.resumeWithException(RuntimeException("Registration failed"))
                }
            }

            override fun onServiceRegistered(p0: NsdServiceInfo?) {
                println("NSD: Service registered: ${p0?.serviceName}")
                if(continuation.isActive) continuation.resume(Unit)
            }

            override fun onServiceUnregistered(p0: NsdServiceInfo?) {
                println("NSD: Service unregistered: ${p0?.serviceName}")
                activeRegistrationListener = null
            }

            override fun onUnregistrationFailed(
                p0: NsdServiceInfo?,
                p1: Int
            ) {
                println("NSD: Unregistration failed for ${p0?.serviceName}: error $p1")

            }
        }

        activeRegistrationListener = registrationListener
        try{
            manager.registerService(serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener)
        } catch (e: Exception){
            activeRegistrationListener = null
            println("NSD: Error registering service: ${e.message}")
            if(continuation.isActive) continuation.resumeWithException(e)
        }

        continuation.invokeOnCancellation {
            unregisterServiceInternal()
        }

    }
    actual suspend fun unregistedService() {
        unregisterServiceInternal()
    }

    private fun unregisterServiceInternal(){
        val manager = nsdManager
        val listner = activeRegistrationListener
        if(manager != null && listner != null){
            try{
                manager.unregisterService(listner)
                println("NSD: Unregister Service Requested")
            } catch (e: Exception){
                println("NSD: Error unregistering service, already unregistered or invalid: ${e.message}")
            } finally {
                activeRegistrationListener = null
            }
        } else {
            activeRegistrationListener = null
        }
    }

    actual fun stopDiscovery() {
        stopPreviousDiscovery()
    }

    fun close(){
        stopDiscovery()
        unregisterServiceInternal()
        println("NSD: Closed")

    }
}