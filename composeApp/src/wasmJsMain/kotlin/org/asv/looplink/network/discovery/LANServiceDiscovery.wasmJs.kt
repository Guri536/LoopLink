package org.asv.looplink.network.discovery

import kotlinx.coroutines.flow.Flow

actual class LANServiceDiscovery {
    actual fun discoverServices(serviceType: String): Flow<List<ServiceInfo>> {
        TODO("Not yet implemented")
    }

    actual suspend fun registerService(
        instanceName: String,
        serviceType: String,
        port: Int,
        attributes: Map<String, String>
    ) {
    }

    actual suspend fun unregistedService() {
    }

    actual fun stopDiscovery() {
    }
}