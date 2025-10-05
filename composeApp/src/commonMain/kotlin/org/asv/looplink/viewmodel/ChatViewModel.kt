package org.asv.looplink.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RoomItem(
    val id: Int,
    val label: String,
    val unread: Int = 0,
)

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
}