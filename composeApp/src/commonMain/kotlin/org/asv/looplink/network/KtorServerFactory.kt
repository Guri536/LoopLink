package org.asv.looplink.network

import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
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
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.User
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.DIRECTORIES
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.ui.RoomItem
import org.asv.looplink.viewmodel.ChatViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal expect fun createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>

fun Application.configureLoopLinkServer(
    chatViewModel: ChatViewModel,
    chatRepository: ChatRepository,
    connectionManager: ConnectionManager,
    userRepository: UserRepository,
    fileRepository: FileRepository
) {
    val userInfo = userRepository.currentUser.value

    install(ContentNegotiation) {
        json(AppJson)
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
                val requesterIp = call.request.origin.remoteAddress
//                if (!connectionManager.isPeerAuthorized(requesterIp)) {
//                    println("Blocked PFP request from non-connected IP: $requesterIp")
//                    call.respond(HttpStatusCode.Unauthorized, "Not authorized.")
//                    return@get
//                }

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

        get("/files/{fileId}") {
            val fileId = call.parameters["fileId"]
            if (fileId == null) {
                call.respond(HttpStatusCode.BadRequest, "File ID is required.")
                return@get
            }

            val requesterIp = call.request.origin.remoteAddress
            if (!connectionManager.isPeerAuthorized(requesterIp)) {
                println("Blocked PFP request from non-connected IP: $requesterIp")
                call.respond(HttpStatusCode.Unauthorized, "Not authorized.")
                return@get
            }

            try {
                // Sanitize again on the server-side as a security measure
                val safeFileId = fileRepository.sanitizeFileName(fileId)
                if (safeFileId != fileId) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid file ID.")
                    return@get
                }

                val fileBytes = fileRepository.getSharedFile(safeFileId)
                val sharedFile = fileRepository.getSharedFileAsFile(safeFileId)
                if (sharedFile != null && sharedFile.exists()) {
                    println("Serving shared file $safeFileId (${sharedFile.length()} bytes)")
                    call.respondOutputStream {
                        sharedFile.inputStream().use { input ->
                            input.copyTo(this)
                        }
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found.")
                }
            } catch (e: Exception) {
                println("Error serving shared file: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Error serving file.")
            }
        }

        webSocket("/looplink/initiate/{roomId}") {
            val roomId = call.parameters["roomId"]?.toIntOrNull()
            val peerUid = call.request.queryParameters["peerUid"]
            val peerName = call.request.queryParameters["peerName"]
            val peerPort = call.request.queryParameters["peerPort"]?.toIntOrNull()
            val peerHost = call.request.origin.remoteAddress

            if (roomId == null || peerUid == null || peerName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid request"))
                return@webSocket
            }

            handlePeerConnection(
                isInitiator = true,
                roomId, peerUid, peerName, peerHost, peerPort, this,
                connectionManager, userRepository, chatRepository, fileRepository, chatViewModel
            )
        }

        webSocket("/looplink/mutual/{roomId}") {
            val params = call.parameters
            val query = call.request.queryParameters
            val peerHost = call.request.origin.remoteAddress

            val roomId = params["roomId"]?.toIntOrNull()
            val peerUid = query["peerUid"]
            val peerName = query["peerName"]
            val peerPort = query["peerPort"]?.toIntOrNull()

            if (roomId == null || peerUid == null || peerName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid request"))
                return@webSocket
            }

            // Call the common handler, marking this as NOT the initiator
            handlePeerConnection(
                isInitiator = false,
                roomId, peerUid, peerName, peerHost, peerPort, this,
                connectionManager, userRepository, chatRepository, fileRepository, chatViewModel
            )
        }
    }
}

private suspend fun handlePeerConnection(
    isInitiator: Boolean,
    roomId: Int,
    peerUid: String,
    peerName: String,
    peerHost: String,
    peerPort: Int?,
    webSocketSession: DefaultWebSocketSession,
    connectionManager: ConnectionManager,
    userRepository: UserRepository,
    chatRepository: ChatRepository,
    fileRepository: FileRepository,
    chatViewModel: ChatViewModel
) {
    connectionManager.addPeer(peerHost)
    println("Server: Peer $peerName ($peerHost) added to connection list.")

    userRepository.addUserToCache(User(peerUid, peerName, null, peerHost, peerPort.toString()))

    val isGroup = chatViewModel.isGroup(roomId)
    if (!chatViewModel.roomExists(roomId)) {
        chatViewModel.addRoom(RoomItem(roomId, label = peerName, isGroup = isGroup))
    }

    // 2. Launch a coroutine for connect-back and PFP download.
    if (peerPort != null && peerPort != 0 && !isGroup) {
        webSocketSession.launch(Dispatchers.IO) {
            try {
                val client = createKtorClient()

                if (isInitiator) {
                    val localUserInfo = userRepository.currentUser.value
                    val localUserPort = userRepository.currentUserPort.value
                    val localUid = localUserInfo?.uid ?: "Unknown"
                    val localName = localUserInfo?.name ?: "Unknown"

                    val encodedUID = URLEncoder.encode(localUid, StandardCharsets.UTF_8.toString())
                    val encodedName = URLEncoder.encode(localName, StandardCharsets.UTF_8.toString())
                    val encodedPort = localUserPort.toString()

                    println("Server: /initiate received. Establishing mutual connection back to $peerName...")
                    val session = client.webSocketSession(
                        method = HttpMethod.Get,
                        host = peerHost,
                        port = peerPort,
                        path = "/looplink/mutual/$roomId?peerUid=$encodedUID&peerName=$encodedName&peerPort=$encodedPort"
                    )
                    chatRepository.addAndListenToClientSession(roomId, session, peerHost)
                    println("Server: Mutual connection to $peerName established.")
                }

                // Fetch the peer's PFP.
                println("Server: Fetching PFP from $peerName at http://$peerHost:$peerPort/user/pfp")
                val pfpBytes: ByteArray = client.get("http://$peerHost:$peerPort/user/pfp").bodyAsBytes()
                val fileModel = fileRepository.copyBlobToFile(pfpBytes, peerUid, DIRECTORIES.ConnDir)!!
                val localPath = fileRepository.getFileInternalPath(fileModel.fileId, fileModel.dir)
                userRepository.updateUserPfpPath(peerUid, localPath)
                chatViewModel.addRoomPfpPath(roomId, localPath)
                println("Server: Successfully downloaded PFP for $peerName.")

            } catch (e: Exception) {
                println("Server: Failed during mutual connection/PFP download for $peerName: ${e.message}")
            }
        }
    }

    chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Connected)
    if(isGroup) chatRepository.addSession(roomId, webSocketSession)

    try {
        // This loop keeps the connection alive
        for (frame in webSocketSession.incoming) {
//            println("Server: Recieved from ${webSocketSession.toString().split('@')[1]} a frame: ${frame.toString()}")
            chatRepository.handleIncomingMessage(roomId, frame, webSocketSession)
        }
    } catch (e: Exception) {
        println("Server: Error in WebSocket session for $peerName: ${e.message}")
    } finally {
        // This now runs correctly when the connection closes
        println("Server: WebSocket connection closed for $peerName ($peerHost)")
        connectionManager.removePeer(peerHost)
        chatRepository.removeSession(roomId, webSocketSession)
        chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Idle)
    }
}
