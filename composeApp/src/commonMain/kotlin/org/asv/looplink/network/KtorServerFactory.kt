package org.asv.looplink.network

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.Message
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.DIRECTORIES
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.ui.RoomItem
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.java.KoinJavaComponent.get
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import org.asv.looplink.components.chat.User
import org.asv.looplink.ui.ConnectionStatus

internal expect fun createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>

fun Application.configureLoopLinkServer(
    chatViewModel: ChatViewModel,
    chatRepository: ChatRepository,
    connectionManager: ConnectionManager,
    userRepository: UserRepository,
    fileRepository: FileRepository
) {
    val user = get<UserRepository>(UserRepository::class.java)
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
        get("/user/pfp") {
            try {
                val pfpPath = userRepository.currentUser.value?.pfpPath
                if (pfpPath == null) {
                    call.respond(HttpStatusCode.NotFound, "PFP for User not set")
                    return@get
                }

                val fileBytes = fileRepository.getFileBytes(pfpPath)
                if (fileBytes == null) {
                    call.respond(HttpStatusCode.NotFound, "PFP file not found at path: $pfpPath")
                    return@get
                }

                println("Serving PFP file from $pfpPath (${fileBytes.size}) bytes")
                call.respondBytes(fileBytes, ContentType.Image.JPEG)

            } catch (e: Exception) {
                println("Error serving PFP file from ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Error Serving File")
            }
        }

        webSocket("/looplink/sync/{roomId}") {
            val roomId = call.parameters["roomId"]?.toIntOrNull()
            val peerUid = call.request.queryParameters["peerUid"]
            val peerName = call.request.queryParameters["peerName"]
            val peerPort = call.request.queryParameters["peerPort"]?.toIntOrNull()
            val peerHost = call.request.origin.remoteAddress

            if (roomId == null || peerUid == null || peerName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid request"))
                return@webSocket
            }

            userRepository.addUserToCache(
                User(
                    peerUid,
                    peerName,
                    null
                )
            )

            if (!chatViewModel.roomExists(roomId)) {
                val newRoom = RoomItem(
                    roomId,
                    label = peerName,
                    members = listOf(userInfo?.uid ?: "Unknown", peerUid)
                )
                chatViewModel.addRoom(newRoom)
            }

            if (peerPort != null && peerPort != 0) {
                launch(Dispatchers.IO) {
                    try {
                        println("Server: Peer $peerName connected, fetching their PFP from http://$peerHost:$peerPort/user/pfp")
                        val client = createKtorClient()
                        val pfpBytes: ByteArray =
                            client.get("http://$peerHost:$peerPort/user/pfp").bodyAsBytes()
                        val localPath = fileRepository.copyBlobToFile(
                            pfpBytes, peerUid,
                            DIRECTORIES.ConnDIR
                        )
                        userRepository.updateUserPfpPath(peerUid, localPath)
                        chatViewModel.addRoomPfpPath(roomId, localPath)
                        println("Server: Successfully downloaded and saved PFP for $peerName at $localPath")
                    } catch (e: Exception) {
                        println("Server: Failed to download PFP from $peerName: ${e.message}")
                    }
                }
            }

            println("Server: New websocket connection for /looplink/sync/$roomId")

            chatRepository.addSession(roomId, this)
            println("KSF: This session: $this")

            chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Connected)

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
                chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Error("Error"))
            } finally {
                println("Server: Websocket connection closed for /looplink/sync")
                chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Idle)
                chatRepository.removeSession(roomId, this)
            }
        }
    }
}
