package org.asv.looplink.network

import android.content.Context
import android.os.Build
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.asv.looplink.network.discovery.LANServiceDiscovery

class AndroidKtorServer(private val context: Context) {
    private var serverEngine: EmbeddedServer<ApplicationEngine, *>? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentPort: Int = 0

    @Volatile // Ensure visibility across threads
    private var isRunning: Boolean = false

    private val serviceDiscovery: LANServiceDiscovery by lazy {
        LANServiceDiscovery(context.applicationContext)
    }

    private val serviceType =
        "_looplink._tcp" // NSD on Android typically doesn't use/need ".local." suffix
    private var serviceInstanceName: String = "LoopLink-${Build.MODEL.replace(" ", "")}"

    var onServerPortChanged: ((Int) -> Unit)? = null

    fun start(
        port: Int = 0,
        instanceName: String? = this.serviceInstanceName
    ) {
        if (isRunning) {
            println("Android Ktor Server is alredy running on port $currentPort")
            onServerPortChanged?.invoke(currentPort)
            return
        }

        this.serviceInstanceName = instanceName ?: this.serviceInstanceName
        val engineFactory = createKtorServerFactory()

        serverJob = serverScope.launch {
            try {
                serverEngine = embeddedServer(
                    factory = engineFactory,
                    port = port,
                    host = "0.0.0.0",
                    module = { configureLoopLinkServer() }
                ).start(wait = false)


                val actualPort =
                    serverEngine?.engine?.resolvedConnectors()?.firstOrNull()?.port ?: port
                currentPort = if (actualPort == 0 && port != 0) port else actualPort

                if (currentPort == 0) {
                    println("Error: Android Server started on port 0")
                }

                isRunning = true
                println("Android Ktor Server Started on port $currentPort")
                onServerPortChanged?.invoke(currentPort)

                serviceDiscovery.registerService(
                    instanceName = this@AndroidKtorServer.serviceInstanceName,
                    serviceType = this@AndroidKtorServer.serviceType,
                    port = currentPort,
                    attributes = mapOf(
                        "deviceId" to "androidDevice-${Build.MODEL.replace(" ", "")}",
                        "platform" to "android"
                    )
                )

                println("Service '${this@AndroidKtorServer.serviceInstanceName}' registered on port $currentPort")

                while (this.isActive) {
                    delay(1000L)
                }
            } catch (e: Exception){
                println("Error starting server: ${e.message}")
            } finally {
                println("Android Ktor Server Coroutine ending")
                if(isRunning){
                    serviceDiscovery.unregistedService()
                    println("Service '${this@AndroidKtorServer.serviceInstanceName}' unregistered")
                }
                serverEngine?.stop(1_000, 5_000)
                isRunning = false
                serverEngine = null
                currentPort = 0
                onServerPortChanged?.invoke(0)
                println("Android Ktor Server stopped")
            }
        }
    }

    fun stop() {
        println("Stopping Android Ktor Server...")
        serverJob?.cancel()
    }

    fun isRunning(): Boolean = isRunning

    fun getCurrentPort(): Int = if (isRunning) currentPort else 0

    fun close(){
        stop()
        serviceDiscovery.close()
        serverScope.cancel()
        println("Android Server closed")
    }

}