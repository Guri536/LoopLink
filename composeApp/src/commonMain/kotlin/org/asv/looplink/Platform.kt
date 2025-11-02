package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import com.db.LLData
import com.db.LLDataQueries
import com.db.LoopLinkUser
import com.db.Room
import org.asv.looplink.components.chat.ManagedFile
import org.asv.looplink.components.chat.Message
import org.asv.looplink.components.chat.MessageType
import org.asv.looplink.components.chat.User
import org.asv.looplink.network.AppJson
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.GroupStructure
import org.asv.looplink.ui.GroupTabs
import org.asv.looplink.ui.GroupType
import org.asv.looplink.ui.RoomItem
import org.asv.looplink.ui.TabType
import java.util.Locale

enum class PlatformType{
    ANDROID,
    DESKTOP
}
interface Platform {
    val name: String
}

expect fun isPlatformMobile(): Boolean
expect fun getPlatformType(): PlatformType

expect fun getPlatform(): Platform

expect class DriverFactory {
    fun createDriver(): SqlDriver
}


class DatabaseManager constructor(private val driver: SqlDriver){
    val database = LLData(driver)
    private val queries = database.lLDataQueries
    fun insertUserData(
        name: String,
        uid: String,
        currentSection: String? = null,
        programCode: String? = null,
        studentContact: String? = null,
        cGPA: String? = null,
        cumail: String? = null,
        pfpPath: String? = null
    ){
        queries.insertLocalUser(
            name.lowercase().capitalize(Locale.UK),
            uid,
            currentSection,
            programCode,
            studentContact,
            cGPA,
            cumail,
            pfpPath
        )
    }

    fun getUserData(): LoopLinkUser = queries.selectLocalUserData().executeAsList()[0]
    fun deleteUser() = queries.deleteLocalUser()
    fun isLoggedIn(): Int = queries.isLoggedIn().executeAsOne().toInt()
    fun updateLocalUserPfp(pfpPath: String?) = queries.updatePfpPath(pfpPath)

    // --- Peer ---
    fun getPeer(uid: String): User? {
        return queries.getPeerByUid(uid).executeAsOneOrNull()?.let {
            User(
                id = it.uid,
                name = it.name,
                pfpPath = it.pfpPath
            )
        }
    }

    fun getAllPeers(): List<User> {
        return queries.getAllPeers().executeAsList().map {
            User(
                id = it.uid,
                name = it.name,
                pfpPath = it.pfpPath
            )
        }
    }

    fun savePeer(user: User) {
        queries.insertPeer(
            uid = user.id,
            name = user.name,
            pfpPath = user.pfpPath
        )
    }

    fun deletePeer(uid: String){
        queries.deletePeer(uid)
    }

    fun updatePeerPfpPath(uid: String, path: String){
        queries.updatePeerPfpPath(path ,uid)
    }

    // --- Message ---

    fun getMessagesFromRoom(roomId: Int): List<Message>{
        return queries.getMessagesForRoom(roomId.toLong()).executeAsList().map { dbMsg ->
            transformDbMessageToMessage(dbMsg)
        }
    }

    fun saveMessage(message: Message) {
        val messageTypeString = when (message.type) {
            else -> message.type.name
        }

        queries.insertMessage(
            id = message.id,
            roomId = message.roomId.toLong(),
            userId = message.userId,
            text = message.text,
            seconds = message.seconds,
            messageType = messageTypeString,
            fileId = message.fileInfo?.fileId,
            fileOriginalName = message.fileInfo?.originalFileName,
            fileMimeType = message.fileInfo?.mimeType,
            fileSizeInBytes = message.fileInfo?.sizeInBytes
        )
    }

    fun getAllMessages(): Map<Int, List<Message>> {
        return queries.getAllMessages().executeAsList().groupBy(
            keySelector = { it.roomId.toInt() },
            valueTransform = { dbMsg ->
                Message(
                    userId = dbMsg.userId,
                    roomId = dbMsg.roomId.toInt(),
                    text = dbMsg.text,
                    seconds = dbMsg.seconds,
                    id = dbMsg.id,
                    type = MessageType.valueOf(dbMsg.messageType),
                    fileInfo = if (dbMsg.fileId != null) {
                        ManagedFile(
                            fileId = dbMsg.fileId,
                            originalFileName = dbMsg.fileOriginalName!!,
                            mimeType = dbMsg.fileMimeType!!,
                            sizeInBytes = dbMsg.fileSizeInBytes!!
                        )
                    } else null
                )
            }
        ).mapValues { it.value.toMutableList() } // Convert to the MutableList your store expects
    }

    // --- Room (Complex operations) ---

    fun getRoom(roomId: Int): RoomItem? {
        val dbRoom = queries.getRoomById(roomId.toLong()).executeAsOneOrNull() ?: return null
        return transformDbRoomToRoomItem(dbRoom)
    }

    fun getAllRooms(): List<RoomItem> {
        return queries.getAllRooms().executeAsList().map { dbRoom ->
            transformDbRoomToRoomItem(dbRoom)
        }
    }

    fun updateRoomTheme(roomId: Int, theme: ChatTheme) {
        val themeJson = AppJson.encodeToString(theme)
        queries.updateRoomTheme(themeJson, roomId.toLong())
    }

    fun updateRoomPfp(roomId: Int, path: String){
        queries.updateRoomPfp(path, roomId.toLong())
    }

    fun updateRoomCustomPfp(roomId: Int, path: String){
        queries.updateRoomCustomPfp(path, roomId.toLong())
    }

    fun saveRoom(room: RoomItem) {
        val themeJson = AppJson.encodeToString(room.chatTheme)

        queries.transaction {
            queries.insertRoom(
                id = room.id.toLong(),
                label = room.label,
                isGroup = room.isGroup,
                pfpPath = room.pfpPath,
                chatThemeJson = themeJson,
                customPfpPath = room.customPfpPath
            )

            room.members.forEach { memberId ->
                queries.insertRoomMember(room.id.toLong(), memberId)
            }

            if (room.isGroup && room.groupDetails != null) {
                val details = room.groupDetails
                queries.insertGroupDetails(
                    roomId = room.id.toLong(),
                    ownerId = details.ownerId,
                    description = details.description,
                    creationTimeStamp = details.creationTimeStamp,
                    groupType = details.groupType.name
                )

                details.admins.forEach { adminId ->
                    queries.insertGroupAdmin(room.id.toLong(), adminId)
                }

                details.tabs.forEach { tab ->
                    queries.insertGroupTab(
                        id = tab.id,
                        roomId = room.id.toLong(),
                        label = tab.label,
                        type = tab.type.name
                    )
                }
            }
        }
    }

    private fun transformDbRoomToRoomItem(dbRoom: Room): RoomItem {
        val members = queries.getMembersForRoom(dbRoom.id).executeAsList()
        val theme = AppJson.decodeFromString<ChatTheme>(dbRoom.chatThemeJson)

        var groupDetails: GroupStructure? = null
        if (dbRoom.isGroup) {
            queries.getGroupDetails(dbRoom.id).executeAsOneOrNull()?.let { dbGroup ->
                val admins = queries.getGroupAdmins(dbGroup.roomId).executeAsList()
                val tabs = queries.getGroupTabs(dbGroup.roomId).executeAsList().map { dbTab ->
                    GroupTabs(
                        id = dbTab.id,
                        label = dbTab.label,
                        type = TabType.valueOf(dbTab.type),
                    )
                }
                groupDetails = GroupStructure(
                    ownerId = dbGroup.ownerId,
                    description = dbGroup.description,
                    admins = admins.toMutableList(),
                    creationTimeStamp = dbGroup.creationTimeStamp,
                    groupType = GroupType.valueOf(dbGroup.groupType),
                    tabs = tabs
                )
            }
        }

        return RoomItem(
            id = dbRoom.id.toInt(),
            label = dbRoom.label,
            isGroup = dbRoom.isGroup,
            pfpPath = dbRoom.pfpPath,
            chatTheme = theme,
            members = members,
            groupDetails = groupDetails
        )
    }

    private fun transformDbMessageToMessage(dbMsg: com.db.Message): Message {
        return Message(
            userId = dbMsg.userId,
            roomId = dbMsg.roomId.toInt(),
            text = dbMsg.text,
            seconds = dbMsg.seconds,
            id = dbMsg.id,
            type = MessageType.valueOf(dbMsg.messageType),
            fileInfo = if (dbMsg.fileId != null) {
                ManagedFile(
                    fileId = dbMsg.fileId,
                    originalFileName = dbMsg.fileOriginalName!!,
                    mimeType = dbMsg.fileMimeType!!,
                    sizeInBytes = dbMsg.fileSizeInBytes!!
                )
            } else null
        )
    }

}

