package org.asv.looplink.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.ui.RoomItem
import org.koin.java.KoinJavaComponent.get

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    private val _rooms = MutableStateFlow<Map<Int, RoomItem>>(emptyMap())
    val rooms = _rooms.asStateFlow()

    val roomsWithStatus: StateFlow<List<RoomItem>> =
        _rooms.combine(chatRepository.activeSessions) { rooms, sessions ->
            rooms.values.map { room ->
                if (sessions.containsKey(room.id)) {
                    room.copy(status = ConnectionStatus.Connected)
                } else {
                    room
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), emptyList<RoomItem>())

    fun addRoom(roomItem: RoomItem) {
        _rooms.update { curRooms ->
            val existingRoom = curRooms.containsKey(roomItem.id)
            if (!existingRoom) {
                curRooms + (roomItem.id to roomItem)
            } else {
                curRooms
            }
        }
    }

    fun updateRoomProperty(roomId: Int, updateProperty: (RoomItem) -> RoomItem) {
        _rooms.update { roomMap ->
            val room = roomMap[roomId] ?: return@update roomMap
            val updatedRoom = updateProperty(room)
            roomMap + (roomId to updatedRoom)
        }
    }

    fun updateRoomConnection(roomId: Int, connectionStatus: ConnectionStatus) {
        updateRoomProperty(roomId){ it.copy(status = connectionStatus) }
    }

    fun roomExists(roomId: Int): Boolean = _rooms.value.containsKey(roomId)


    fun updateRoomTheme(roomId: Int, newTheme: ChatTheme) {
        updateRoomProperty(roomId){ it.copy(chatTheme = newTheme) }
    }

    fun getRoomTheme(roomId: Int): ChatTheme? = _rooms.value[roomId]?.chatTheme
    fun getRoomLabel(roomId: Int): String? = _rooms.value[roomId]?.label
    fun getRoomStatus(roomId: Int): ConnectionStatus? = _rooms.value[roomId]?.status
    fun getRoom(roomId: Int): RoomItem? = _rooms.value[roomId]
    fun getPfp(roomId: Int): String? = _rooms.value[roomId]?.pfpPath ?: _rooms.value[roomId]?.groupDetails?.pfpPath
    fun getPeerDefaultColor(roomId: Int): Color = _rooms.value[roomId]?.chatTheme?.defaultPeerColor!!

    fun setBackgroundImage(roomId: Int, newSourcePath: String) {
        val fileRepository: FileRepository = get(FileRepository::class.java)
        println("Got: $newSourcePath")
        viewModelScope.launch {
            val newManagedFile = fileRepository.copyFileToInternalStorage(newSourcePath)
            println(newManagedFile?.internalPath)
            val currentTheme = getRoomTheme(roomId) ?: ChatTheme.default()
            val currentBackgroundImage = currentTheme.backgroundImagePath

            if (currentBackgroundImage != null && currentBackgroundImage != newManagedFile?.internalPath) {
                null
            }

            val updataedTheme = currentTheme.copy(
                backgroundImagePath = newManagedFile?.internalPath
            )
            println("New theme back: ${updataedTheme.backgroundImagePath}")
            updateRoomTheme(roomId, updataedTheme)

//            cleanupOrphanFiles()
        }
    }

    private suspend fun cleanupOrphanFiles() {
        TODO()
    }
}