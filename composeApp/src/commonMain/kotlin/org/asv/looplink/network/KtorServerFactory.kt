package org.asv.looplink.network

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket // Added for webSocket route
import io.ktor.websocket.Frame // Added for Frame
import io.ktor.websocket.readText // Added for readText
import kotlinx.serialization.json.Json

internal expect fun createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>

fun Application.configureLoopLinkServer() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }

    install(WebSockets) {
//        pingPeriodMillis = 60_000 // Disabled (null) by default
        timeoutMillis = 15_000
        maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }

    routing {
        get("/") {
            call.respondText("Hello there!")
        }
        get("/android"){
            call.respondText("Hello from Android!")
        }

        webSocket("/looplink/sync") {
            println("Server: New websocket connection for /looplink/sync")
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val recievedText = frame.readText()
                        println("Server recieved from client: $recievedText")

                        val responseText = "Hello from server!"
                        send(Frame.Text(responseText))
                    }
                }
            } catch (e: Exception) {
                println("Error in websocket: ${e.message}")
            } finally {
                println("Server: Websocket connection closed for /looplink/sync")
            }
        }
    }
}
