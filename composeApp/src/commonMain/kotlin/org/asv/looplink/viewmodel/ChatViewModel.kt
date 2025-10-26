package org.asv.looplink.viewmodel

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

class ChatViewModel(private val chatRepository: ChatRepository): ViewModel() {
    private val _rooms = MutableStateFlow<List<RoomItem>>(emptyList())
    val rooms = _rooms.asStateFlow()

    val roomsWithStatus: StateFlow<List<RoomItem>> =
        _rooms.combine(chatRepository.activeSessions){ rooms, sessions ->
            rooms.map{ room ->
                if(sessions.containsKey(room.id)){
                    room.copyMe(status = ConnectionStatus.Connected)
                } else {
                    room
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), emptyList<RoomItem>())

    fun addRoom(roomItem: RoomItem) {
        _rooms.update { curRooms ->
            val existingRoom = curRooms.find { it.id == roomItem.id }
            if (existingRoom == null) {
                curRooms + roomItem
            } else {
                curRooms
            }
        }
    }

    fun updateRoomConnection(roomId: Int, connectionStatus: ConnectionStatus){
        _rooms.update { curRoom ->
            curRoom.map{ room ->
                if(room.id == roomId){
                    room.copyMe(status = connectionStatus)
                } else {
                    room
                }
            }
        }
    }

    fun roomExists(roomId: Int): Boolean {
        return _rooms.value.find { it.id == roomId } != null
    }

    fun updateRoomTheme(roomId: Int, newTheme: ChatTheme){
        _rooms.update { rooms ->
            rooms.map { room ->
                if(room.id == roomId){
                    room.copyMe(chatTheme = newTheme)
                } else {
                    room
                }
            }
        }
    }

    fun getRoomTheme(roomId: Int): ChatTheme? {
        _rooms.value.forEach { room ->
            if(room.id == roomId) return room.chatTheme
        }
        return null
    }

    fun getRoomLabel(roomId: Int): String?{
        _rooms.value.forEach { room ->
            if(room.id == roomId) return room.label
        }
        return null
    }

    fun getRoomStatus(roomId: Int): ConnectionStatus{
        _rooms.value.forEach { room ->
            if(room.id == roomId) return room.status
        }
        return ConnectionStatus.Idle
    }

    fun getRoom(roomId: Int): RoomItem? {
        _rooms.value.forEach { room ->
            if(room.id == roomId) return room
        }
        return null
    }


    fun setBackgroundImage(roomId: Int, newSourcePath: String){
        val fileRepository: FileRepository = get(FileRepository::class.java)
        println("Got: $newSourcePath")
        viewModelScope.launch {
            val newManagedFile = fileRepository.copyFileToInternalStorage(newSourcePath)
            println(newManagedFile?.internalPath)
            val currentTheme = getRoomTheme(roomId) ?: ChatTheme.default()
            val currentBackgroundImage = currentTheme.backgroundImagePath

            if(currentBackgroundImage != null && currentBackgroundImage != newManagedFile?.internalPath){
                null
            }

            val updataedTheme = currentTheme.copyMe(
                backgroundImagePath = newManagedFile?.internalPath
            )
            println("New theme back: ${updataedTheme.backgroundImagePath}")
            updateRoomTheme(roomId, updataedTheme)

//            cleanupOrphanFiles()
        }
    }

    private suspend fun cleanupOrphanFiles(){
        TODO()
    }
}