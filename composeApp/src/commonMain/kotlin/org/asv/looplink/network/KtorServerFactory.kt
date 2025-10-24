package org.asv.looplink.network

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.Message
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.viewmodel.RoomItem
import java.util.Collections
import org.koin.java.KoinJavaComponent.get

internal expect fun createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>

fun Application.configureLoopLinkServer(
    chatViewModel: ChatViewModel,
    chatRepository: ChatRepository,
    connectionManager: ConnectionManager
) {
    val user = get<UserRespository>(UserRespository::class.java)
    val userInfo = user.currentUser.value

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }

    install(WebSockets) {
        timeoutMillis = 15_000
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondText("Hello there!")
        }
        get("/android") {
            call.respondText("Hello from Android!")
        }

        webSocket("/looplink/sync/{roomId}") {
            val roomId = call.parameters["roomId"]?.toIntOrNull()
            val peerUid = call.request.queryParameters["peerUid"]
            val peerName = call.request.queryParameters["peerName"]

            if (roomId == null || peerUid == null || peerName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid request"))
                return@webSocket
            }

            if (!chatViewModel.roomExists(roomId)) {
                val newRoom = RoomItem(
                    roomId,
                    label = peerName,
                    members = listOf(userInfo?.uid ?: "Unknown", peerUid)
                )
                chatViewModel.addRoom(newRoom)
            }

            println("Server: New websocket connection for /looplink/sync/$roomId")

            chatRepository.addSession(roomId, this)
            println("KSF: This session: $this")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        println("Server received from client: ${receivedText.take(50)}")
                        try {
                            val message = Json.decodeFromString<Message>(receivedText)
                            chatRepository.store.send(Action.SendMessage(roomId, message))

                            connectionManager.broadcast(roomId, receivedText, this)
                        } catch (e: Exception) {
                            println("Error parsing message: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error in websocket: ${e.message}")
            } finally {
                println("Server: Websocket connection closed for /looplink/sync")
                chatRepository.removeSession(roomId, this)
            }
        }
    }
}
