package org.asv.looplink.network

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.isActive
import org.asv.looplink.data.repository.ChatRepository

class ConnectionManager(private val chatRepository: ChatRepository){
    suspend fun broadcast(roomId: Int, message: String, sender: DefaultWebSocketSession){
        chatRepository.activeSessions.value[roomId]?.forEach {
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