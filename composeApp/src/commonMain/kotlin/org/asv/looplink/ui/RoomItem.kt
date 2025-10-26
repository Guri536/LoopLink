package org.asv.looplink.ui

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.asv.looplink.theme.ChatTheme
import java.util.UUID

expect sealed class ConnectionStatus() {
    object Idle : ConnectionStatus
    object Connecting : ConnectionStatus
    object Connected : ConnectionStatus
    object Error : ConnectionStatus
}
expect enum class GroupType {
    GENERAL,
    PROJECT,
    CLASS,
    LAB
}
expect enum class TabType {
    CHAT, PANELS, ASSIGNMENTS, FILES, WHITEBOARD
}

expect class GroupTabs(
    id: String = UUID.randomUUID().toString(),
    label: String,
    type: TabType
) {
    val id: String
    var label: String
    val type: TabType
}

expect class GroupStructure(
    ownerId: String,
    description: String? = null,
    admins: MutableList<String> = mutableListOf<String>(),
    creationTimeStamp: Long,
    groupType: GroupType = GroupType.GENERAL,
    tabs: List<GroupTabs> = listOf()
) {
    val ownerId: String
    var description: String?
    val admins: MutableList<String>
    val creationTimeStamp: Long
    val groupType: GroupType
    val tabs: List<GroupTabs>
}


expect class RoomItem(
    id: Int,
    label: String,
    unread: Int = 0,
    isGroup: Boolean = false,
    groupDetails: GroupStructure? = null,
    members: List<String> = emptyList(),
    status: ConnectionStatus = ConnectionStatus.Idle,
    chatTheme: ChatTheme? = ChatTheme()
) {
    var chatTheme: ChatTheme?
    val id: Int
    val label: String
    val unread: Int
    val isGroup: Boolean
    val groupDetails: GroupStructure?
    val members: List<String>
    val status: ConnectionStatus

    fun copyMe(
        id: Int = this.id,
        label: String = this.label,
        unread: Int = this.unread,
        isGroup: Boolean = this.isGroup,
        groupDetails: GroupStructure? = this.groupDetails,
        members: List<String> = this.members,
        status: ConnectionStatus = this.status,
        chatTheme: ChatTheme = this.chatTheme!!
    ): RoomItem
}