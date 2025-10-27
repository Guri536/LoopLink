package org.asv.looplink.ui

import kotlinx.serialization.Serializable
import org.asv.looplink.theme.ChatTheme
import java.util.UUID

@Serializable
sealed class ConnectionStatus {
    object Idle : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

@Serializable
enum class GroupType {
    GENERAL, PROJECT, CLASS, LAB
}

@Serializable
enum class TabType {
    CHAT, PANELS, ASSIGNMENTS, FILES, WHITEBOARD
}

@Serializable
data class GroupTabs(
    val id: String = UUID.randomUUID().toString(),
    var label: String,
    val type: TabType
)

@Serializable
data class GroupStructure(
    val ownerId: String,
    var description: String? = null,
    val admins: MutableList<String> = mutableListOf<String>(ownerId),
    val creationTimeStamp: Long,
    val groupType: GroupType = GroupType.GENERAL,
    val tabs: List<GroupTabs> = listOf(GroupTabs(label = "Chat", type = TabType.CHAT))
)

@Serializable
data class RoomItem(
    val id: Int,
    val label: String,
    val unread: Int = 0,
    val isGroup: Boolean = false,
    val groupDetails: GroupStructure? = null,
    val members: List<String> = emptyList(),
    val status: ConnectionStatus = ConnectionStatus.Idle,
    var chatTheme: ChatTheme = ChatTheme.default()
)