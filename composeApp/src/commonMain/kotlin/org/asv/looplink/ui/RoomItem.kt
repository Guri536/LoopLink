package org.asv.looplink.viewmodel

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectionStatus {
    object Idle : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}
@Serializable
data class RoomItem(
    val id: Int,
    val label: String,
    val unread: Int = 0,
    val isGroup: Boolean = false,
    val members: List<String> = emptyList(),
    val status: ConnectionStatus = ConnectionStatus.Idle
)
