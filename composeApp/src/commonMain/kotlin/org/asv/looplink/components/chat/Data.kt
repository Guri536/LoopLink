package org.asv.looplink.components.chat

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random
import kotlin.random.nextInt

@Serializable
data class User(
    var name: String,
    var picture: ByteArray?,
    @Transient val color: Color = ColorProvider.getColor(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (name != other.name) return false
        if (picture != null) {
            if (other.picture == null) return false
            if (!picture.contentEquals(other.picture)) return false
        } else if (other.picture != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class Message(
    val user: User,
    val text: String,
    val seconds: Long,
    val id: Long
) {
    constructor(
        user: User,
        text: String
    ) : this(
        user = user,
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
        if(colors.isEmpty()) {
            colors.addAll(allColors)
        }
        val idx = Random.nextInt(colors.indices)
        val color = colors[idx]
        colors.removeAt(idx)
        return Color(color)
    }
}