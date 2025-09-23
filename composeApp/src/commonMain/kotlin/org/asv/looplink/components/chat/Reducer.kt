package org.asv.looplink.components.chat

sealed interface Action {
    data class SendMessage(val roomId: Int, val message: Message) : Action
    data class LoadRoom(val roomId: Int) : Action
}

data class State(
    val rooms: Map<Int, MutableList<Message>> = emptyMap()
)

fun chatReducer(state: State, action: Action): State =
    when (action) {
        is Action.SendMessage -> {
            val updatedMessages =
                (state.rooms[action.roomId].orEmpty() + action.message).takeLast(100) as MutableList
            state.copy(
                rooms = state.rooms + (action.roomId to updatedMessages)
            )
        }

        is Action.LoadRoom -> {
            state
        }
    }
