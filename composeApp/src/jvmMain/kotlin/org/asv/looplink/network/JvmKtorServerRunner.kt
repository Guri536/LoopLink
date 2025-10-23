package org.asv.looplink.network

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel

object jvmKtorServerRunner{
    private var serverEngine: EmbeddedServer<ApplicationEngine, *>? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentPort = 8080
    private var isRunning: Boolean = false

    private val serviceDiscovery: LANServiceDiscovery by lazy{
        LANServiceDiscovery().apply { initialize() }
    }

    private const val SERVICE_TYPE = "_looplink._tcp"
    private var serviceInstanceName = "LoopLinkJVM-${System.currentTimeMillis()}"

    fun start(
        port: Int = 0,
        userUid: String,
        userName: String,
        chatViewModel: ChatViewModel,
        peerDiscoveryViewModel: PeerDiscoveryViewModel?
    ): Int {
        if(isRunning){
            println("Server already running on $currentPort")
            return currentPort
        }

        val instanceName = "LoopLink-$userName"
        this.serviceInstanceName = instanceName
        val engineFactory = createKtorServerFactory()

        serverJob = serverScope.launch {
            try{
                serverEngine = embeddedServer(
                    factory = engineFactory,
                    port = port,
                    host = "0.0.0.0",
                    module = { configureLoopLinkServer(chatViewModel, peerDiscoveryViewModel!!) }
                ).start(wait = false)

                currentPort = serverEngine?.engine?.resolvedConnectors()?.firstOrNull()?.port ?: 0
                if (currentPort == 0 && port != 0) currentPort = port
                if(currentPort == 0){
                    println("Error starting sever on port 0")
                }

                isRunning = true
                println("JVM Ktor server started on port $currentPort")

                serviceDiscovery.registerService(
                    instanceName = this@jvmKtorServerRunner.serviceInstanceName,
                    serviceType = SERVICE_TYPE,
                    port = currentPort,
                    attributes = mapOf(
                        "uid" to userUid,
                        "name" to userName,
                        "platform" to "jvm"
                    )
                )

                while(this.isActive && serverEngine?.application?.isActive == true){
                    delay(100L)
                }

            } catch (e: Exception) {
                println("Error starting server: ${e.message}")
                stop()
            } finally {
                println("JVM Ktor Server Coroutine ending")
                println("Stopping JVM Ktor Server...")
                if (isRunning) {
                    serviceDiscovery.unregisterService()
                    serverEngine?.stop(1_000, 5_000)
                    isRunning = false
                    serverEngine = null
                    currentPort = 0
                    println("Service '${this@jvmKtorServerRunner.serviceInstanceName}' unregistered")
                }
            }
        }

        return if(port == 0){
            println("Server Starting on random port")
            0
        } else {
            port
        }
    }

    fun stop(){
        serverJob?.cancel()

        println("JVM Ktor Server stopped")
    }

    fun isRunning(): Boolean = isRunning

    fun close(){
        stop()
        serviceDiscovery.close()
        serverScope.cancel()
        println("JVM Ktor Server closed")
    }
}
