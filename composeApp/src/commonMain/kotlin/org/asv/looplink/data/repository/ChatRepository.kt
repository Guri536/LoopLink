package org.asv.looplink.data.repository

import androidx.compose.runtime.collectAsState
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.asv.looplink.components.chat.createStore
import kotlin.collections.plus
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.DatabaseManager
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.Action.*
import org.asv.looplink.components.chat.GroupInviteEvent
import org.asv.looplink.components.chat.LoopLinkEvent
import org.asv.looplink.components.chat.Message
import org.asv.looplink.components.chat.TypingEvent
import org.asv.looplink.network.AppJson
import org.asv.looplink.network.ConnectionManager
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.java.KoinJavaComponent.get

class ChatRepository(
    private val database: DatabaseManager
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())
    val store = coroutineScope.createStore()

    private val _activeSessions = MutableStateFlow<Map<Int, Set<DefaultWebSocketSession>>>(emptyMap())
    val activeSessions = _activeSessions.asStateFlow()

    fun handleIncomingMessage(roomId: Int, frame: Frame, senderSession: DefaultWebSocketSession) {
        val chatViewModel: ChatViewModel = get(ChatViewModel::class.java)
        if (frame is Frame.Text) {
            val receivedText = frame.readText()
            try {
                val event = AppJson.decodeFromString<LoopLinkEvent>(receivedText)
                when (event) {
                    is Message -> {
                        chatViewModel.onMessageReceived(roomId)
                        println("ChatRepo: Added message to store for $roomId and incremented unread count")
                        store.send(SendMessage(roomId = roomId, message = event))

                        database.saveMessage(event)

                        if (chatViewModel.isGroup(roomId)) {
                            println("ChatRepo: Broadcasting group message for room $roomId")
                            coroutineScope.launch {
                                broadcast(roomId, receivedText, senderSession)
                            }
                        }
                    }
                    is TypingEvent -> {
                        chatViewModel.onTypingEvent(event.roomId, event.userId, event.isTyping)
                        if (chatViewModel.isGroup(roomId)) {
                            coroutineScope.launch {
                                broadcast(roomId, receivedText, senderSession)
                            }
                        }
                    }

                    is GroupInviteEvent -> {
                        println("ChatRepo: Received group invite!")
                        chatViewModel.onGroupInviteReceived(event)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ChatRepo: Error decoding message for room: $roomId")
            }
        }
    }

    fun getLastMessage(roomId: Int): Flow<Message?> {
        return store.stateFlow.map { state ->
            state.rooms[roomId]?.lastOrNull()
        }
    }
    suspend fun sendMessage(roomId: Int, message: String){
        _activeSessions.value[roomId]?.forEach { session ->
            println("Sending text to ${session.toString()}")
            session.send(Frame.Text(message))
        }
    }

    fun addAndListenToClientSession(roomId: Int, session: DefaultWebSocketSession, host: String) {
        addSession(roomId, session) // Use your existing function to add it

        println("ChatRepo: [Client] Starting to listen to ${session.toString().split('@')[1]} for room: $roomId")

        // This launches the listener in the repository's scope
        session.incoming.consumeAsFlow().onEach { frame ->
            handleIncomingMessage(roomId, frame, session) // Use your existing handler
        }.catch { e ->
            // This 'catch' block acts as the 'finally' for the client-side
            println("ChatRepo: [Client] Error/Closed session for room $roomId: ${e.message}")
            val chatViewModel: ChatViewModel = get(ChatViewModel::class.java)
            val connectionManager: ConnectionManager = get(ConnectionManager::class.java)

            // Clean up resources for this connection
            connectionManager.removePeer(host)
            chatViewModel.updateRoomConnection(roomId, ConnectionStatus.Idle)
            removeSession(roomId, session)
        }.launchIn(coroutineScope)
    }

    suspend fun broadcast(roomId: Int, message: String, sender: DefaultWebSocketSession){
        activeSessions.value[roomId]?.forEach {
                session ->
            if(session.isActive && session != sender){
                try{
                    session.send(Frame.Text(message))
                } catch (e: Exception) {
                    println("ConnectionManager: Error broadcasting to session: ${e.message}")
                }
            }
        }
    }

    fun addSession(roomId: Int, session: DefaultWebSocketSession) {
        val currentSessions = _activeSessions.value[roomId] ?: emptySet<DefaultWebSocketSession>()
        val updatedSessions = currentSessions + session
        _activeSessions.update { it + (roomId to updatedSessions) }
        println("ChatRepo: Session added room $roomId to active sessions.")
    }

    fun removeSession(roomId: Int, session: DefaultWebSocketSession){
        val currentSessions = _activeSessions.value[roomId] ?: return
        val updatedSessions = currentSessions - session

        _activeSessions.update { currentMap ->
            if(updatedSessions.isEmpty()){
                currentMap - roomId
            } else {
                currentMap + (roomId to updatedSessions)
            }
        }
        println("ChatRepo: Session removed for room $roomId")

        coroutineScope.launch { session.close() }
    }
}