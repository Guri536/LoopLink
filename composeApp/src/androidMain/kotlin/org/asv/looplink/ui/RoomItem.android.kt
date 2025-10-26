package org.asv.looplink.ui

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.typesalias.KmpIgnoreOnParcel
import org.asv.looplink.typesalias.KmpParcelable
import java.util.UUID

@Serializable
@Parcelize
actual sealed class ConnectionStatus: Parcelable {
    @Parcelize
    @Serializable
    actual object Idle : ConnectionStatus(), Parcelable
    @Parcelize
    @Serializable
    actual object Connecting : ConnectionStatus(), Parcelable
    @Parcelize
    @Serializable
    actual object Connected : ConnectionStatus(), Parcelable
    @Parcelize
    @Serializable
    actual object Error : ConnectionStatus(), Parcelable
}

@Parcelize
@Serializable
actual enum class GroupType: Parcelable {
    GENERAL,
    PROJECT,
    CLASS,
    LAB
}
@Parcelize
@Serializable
actual class GroupTabs actual constructor(
    actual val id: String,
    actual var label: String,
    actual val type: TabType
): Parcelable
@Parcelize
@Serializable
actual enum class TabType: Parcelable {
    CHAT, PANELS, ASSIGNMENTS, FILES, WHITEBOARD
}

@Parcelize
@Serializable
actual class GroupStructure actual constructor(
    actual val ownerId: String,
    actual var description: String?,
    actual val admins: MutableList<String>,
    actual val creationTimeStamp: Long,
    actual val groupType: GroupType,
    actual val tabs: List<GroupTabs>
) : Parcelable

@Parcelize
@Serializable
actual data class RoomItem actual constructor (
    actual val id: Int,
    actual val label: String,
    actual val unread: Int,
    actual val isGroup: Boolean,
    actual val groupDetails: GroupStructure?,
    actual val members: List<String>,
    actual val status: ConnectionStatus,
    actual var chatTheme: ChatTheme?
): Parcelable {
    actual fun copyMe(
        id: Int,
        label: String,
        unread: Int,
        isGroup: Boolean,
        groupDetails: GroupStructure?,
        members: List<String>,
        status: ConnectionStatus,
        chatTheme: ChatTheme
    ): RoomItem {
        return RoomItem(
            id,label,unread,isGroup,groupDetails,members,status,chatTheme
        )
    }
}