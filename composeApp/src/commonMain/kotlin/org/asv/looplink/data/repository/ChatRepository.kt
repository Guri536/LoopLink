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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.Message

class ChatRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())
    val store = coroutineScope.createStore()

    private val _activeSessions = MutableStateFlow<Map<Int, DefaultWebSocketSession>>(emptyMap())
    val activeSessions = _activeSessions.asStateFlow()

    fun addAndListenToSession(roomId: Int, session: DefaultWebSocketSession){
        addSession(roomId, session)
        listenToSession(roomId, session)
    }

    private fun listenToSession(roomId: Int, session: DefaultWebSocketSession) {
        println("ChatRepo: Starting to listening to ${session.toString().split('@').get(1)} for room: $roomId")

        session.incoming.consumeAsFlow().onEach {
            frame ->
            if(frame is Frame.Text){
                val receivedText = frame.readText()
                try{
                    val message = Json.decodeFromString<Message>(receivedText)
                    store.send(Action.SendMessage(roomId = roomId, message = message))
                } catch (e: Exception){
                    e.printStackTrace()
                    println("ChatRepo: Error decoding message for room: $roomId")
                }
            }
        }.catch{
            e ->
            println("ChatRepo: Error in incoming flow for room $roomId: ${e.message}")
        }.launchIn(coroutineScope)
    }

    fun addSession(roomId: Int, session: DefaultWebSocketSession) {
        _activeSessions.update { it + (roomId to session) }
        println("ChatRepo: Session added room $roomId to active sessions.")
    }

    fun removeSession(roomId: Int){
        _activeSessions.value[roomId]?.let{
            session ->
            coroutineScope.launch { session.close() }
        }
        _activeSessions.update { it - roomId }
        println("ChatRepo: Session removed for room $roomId")
    }
}