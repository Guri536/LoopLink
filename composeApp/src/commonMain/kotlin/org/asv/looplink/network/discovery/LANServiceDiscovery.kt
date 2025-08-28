package org.asv.looplink.network.discovery

import kotlinx.coroutines.flow.Flow

expect class LANServiceDiscovery constructor(){
    fun discoverServices(serviceType: String): Flow<List<ServiceInfo>>
    suspend fun registerService(
        instanceName: String,
        serviceType: String,
        port: Int,
        attributes: Map<String, String>
    )

    suspend fun unregistedService()

    fun stopDiscovery()
    fun stopDiscovery(serviceType: String?)
}