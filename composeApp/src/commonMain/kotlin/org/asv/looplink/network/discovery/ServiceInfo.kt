package org.asv.looplink.network.discovery

data class ServiceInfo (
    val instanceName: String,
    val serviceName: String,
    val hostAddress: String,
    val port: Int,
    val attributes: Map<String, String>
)