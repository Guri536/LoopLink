package org.asv.looplink.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel: ViewModel() {
    private val roomsFlow = MutableStateFlow<List<RoomItem>>(emptyList())
    val rooms = roomsFlow.asStateFlow()

    fun addRoom(roomItem: RoomItem) {
        roomsFlow.update { curRooms ->
            val existingRoom = curRooms.find { it.id == roomItem.id }
            if (existingRoom == null) {
                curRooms + roomItem
            } else {
                curRooms
            }
        }
    }

    fun updateRoomConnection(roomId: Int, connectionStatus: ConnectionStatus){
        roomsFlow.update { curRoom ->
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
        return roomsFlow.value.find { it.id == roomId } != null
    }
}