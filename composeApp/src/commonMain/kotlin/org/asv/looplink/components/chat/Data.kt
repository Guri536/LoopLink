package org.asv.looplink.components.chat

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.asv.looplink.data.repository.DIRECTORIES
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class User(
    val id: String,
    val name: String,
    val pfpPath: String? = null,
    @Transient val hostAddress: String? = null,
    @Transient val port: String? = null
)

@Serializable
data class ManagedFile(
    val fileId: String,
    val originalFileName: String,
    val mimeType: String,
    val sizeInBytes: Long,
    val dir: DIRECTORIES = DIRECTORIES.FilesDir
)

@Serializable
data class Message(
    val userId: String,
    val roomId: Int,
    val text: String? = null,
    val seconds: Long,
    val id: Long,
    val type: MessageType = MessageType.TEXT,
    val fileInfo: ManagedFile? = null
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

    @OptIn(ExperimentalTime::class)
    constructor(
        userId: String,
        roomId: Int,
        fileInfo: ManagedFile,
        text: String? = null
    ): this(
        userId = userId,
        roomId = roomId,
        seconds = Clock.System.now().epochSeconds,
        text = text,
        id = Random.nextLong(),
        type = MessageType.FILE,
        fileInfo = fileInfo
    )
}

@Serializable
enum class MessageType{
    TEXT,
    FILE
}

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