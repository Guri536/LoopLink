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
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.viewmodel.ConnectionStatus

class ChatViewModel(private val chatRepository: ChatRepository): ViewModel() {
    private val _rooms = MutableStateFlow<List<RoomItem>>(emptyList())
    val rooms = _rooms.asStateFlow()

    val roomsWithStatus: StateFlow<List<RoomItem>> =
        _rooms.combine(chatRepository.activeSessions){ rooms, sessions ->
            rooms.map{ room ->
                if(sessions.containsKey(room.id)){
                    room.copy(status = ConnectionStatus.Connected)
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
                    room.copy(status = connectionStatus)
                } else {
                    room
                }
            }
        }
    }

    fun roomExists(roomId: Int): Boolean {
        return _rooms.value.find { it.id == roomId } != null
    }
}