package org.asv.looplink.network

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Message
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.viewmodel.RoomItem
import org.koin.java.KoinJavaComponent.get
import java.util.Collections

class ConnectionManager {
    private val connections = Collections.synchronizedMap<Int, MutableSet<DefaultWebSocketSession>>(mutableMapOf())
    private val chatRepository: ChatRepository = get(ChatRepository::class.java)

    fun addConnection(roomId: Int, session: DefaultWebSocketSession){
        val roomConnections = connections.computeIfAbsent(roomId){
            Collections.synchronizedSet<DefaultWebSocketSession>(
                LinkedHashSet()
            )
        }
        roomConnections.add(session)
        println("ConnectionManager: Session added to room $roomId. Total sessions in room: ${roomConnections.size}")
        chatRepository.addAndListenToSession(roomId, session)
    }

    fun removeConnection(roomId: Int, session: DefaultWebSocketSession){
        connections[roomId]?.let{ roomConnections ->
            roomConnections.remove(session)
            println("ConnectionManager: Session removed from room $roomId. Total sessions in room: ${roomConnections.size}")
        }
    }

    suspend fun broadcast(roomId: Int, message: String, sender: DefaultWebSocketSession){
        connections[roomId]?.forEach {
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
}