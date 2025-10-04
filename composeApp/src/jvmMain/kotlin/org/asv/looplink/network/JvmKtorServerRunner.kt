package org.asv.looplink.network


import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.asv.looplink.network.discovery.LANServiceDiscovery

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
        instanceName: String = serviceInstanceName,
//        serviceDiscovery: LANServiceDiscovery
    ): Int {
        if(isRunning){
            println("Server already running on $currentPort")
            return currentPort
        }

        this.serviceInstanceName = instanceName
        val engineFactory = createKtorServerFactory()

        serverJob = serverScope.launch {
            try{
                serverEngine = embeddedServer(
                    factory = engineFactory,
                    port = port,
                    host = "0.0.0.0",
                    module = { configureLoopLinkServer() }
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
                    attributes = mapOf("deviceId" to "jvmDevice-${System.getProperty("user.name")}", "platform" to "jvm")
                )
//                println("Service '${this@jvmKtorServerRunner.serviceInstanceName}' registered on port $currentPort")

                while(this.isActive && serverEngine?.application?.isActive == true){
                    delay(100L)
                }

            } catch (e: Exception) {
                println("Error starting server: ${e.message}")
                stop()
            } finally {
                println("JVM Ktor Server Coroutine ending")
                if(isRunning){
                    serviceDiscovery.unregisterService()
                    println("Service '${this@jvmKtorServerRunner.serviceInstanceName}' unregistered")
                }
                serverEngine?.stop(1_000, 5_000)
                isRunning = false
                serverEngine = null
                currentPort = 0
                println("JVM Ktor Server stopped")
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
        println("Stopping JVM Ktor Server...")
        serverJob?.cancel()
    }

    fun isRunning(): Boolean = isRunning

    fun getCurrentPort(): Int = if(isRunning) currentPort else 0

//    fun closeDiscovery(){
//        serviceDiscovery.close()
//    }
}