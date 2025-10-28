package org.asv.looplink.components.chat

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class User(
    val id: String,
    val name: String,
    val pfpPath: String? = null
)

@Serializable
data class Message(
    val userId: String,
    val roomId: Int,
    val text: String,
    val seconds: Long,
    val id: Long
) {
    @OptIn(ExperimentalTime::class)
    constructor(
        userId: String,
        roomId: Int,
        text: String
    ) : this(
        userId = userId,
        roomId = roomId,
        text = text,
        seconds = Clock.System.now().epochSeconds,
        id = Random.nextLong()
    )

}

data class MessageList(
    val messages: MutableList<Message> = mutableListOf<Message>()
)

object ColorProvider {
    val colors = mutableListOf(
        0xFFEA3468,
        0xFFB634EA,
        0xFF349BEA,
    )
    val allColors = colors.toList()
    fun getColor(): Color {
        if (colors.isEmpty()) {
            colors.addAll(allColors)
        }
        val idx = Random.nextInt(colors.indices)
        val color = colors[idx]
        colors.removeAt(idx)
        return Color(color)
    }
}