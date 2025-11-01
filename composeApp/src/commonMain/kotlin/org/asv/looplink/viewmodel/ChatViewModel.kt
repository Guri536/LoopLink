package org.asv.looplink.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsBytes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.LoopLinkEvent
import org.asv.looplink.components.chat.Message
import org.asv.looplink.components.chat.MessageType
import org.asv.looplink.components.chat.TypingEvent
import org.asv.looplink.components.chat.User
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.DIRECTORIES
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.network.AppJson
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.ui.RoomItem
import org.koin.java.KoinJavaComponent.get
import java.io.File

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _rooms = MutableStateFlow<Map<Int, RoomItem>>(emptyMap())
    val rooms = _rooms.asStateFlow()

    private val _activeRoomId = MutableStateFlow<Int?>(null)
    val activeRoomId = _activeRoomId.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<Int, Set<String>>>(emptyMap())
    val typingUsers = _typingUsers.asStateFlow()

    private val stopTypingJobs = mutableMapOf<Int, Job>()

    fun onTypingEvent(roomId: Int, userId: String, isTyping: Boolean) {
        _typingUsers.update { currentMap ->
            val currentTypingUsers = currentMap[roomId] ?: emptySet()
            val newTypingUsers = if (isTyping) {
                currentTypingUsers + userId
            } else {
                currentTypingUsers - userId
            }
            // If the set is empty, remove the key from the map
            if (newTypingUsers.isEmpty()) {
                currentMap - roomId
            } else {
                currentMap + (roomId to newTypingUsers)
            }
        }
    }

    // ADDED: Function called by SendMessage.kt when the user types
    fun sendTypingEvent(roomId: Int, isTyping: Boolean) {
        // Cancel any pending "stop typing" job for this room
        stopTypingJobs[roomId]?.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = userRepository.getUserIdAndName().first ?: return@launch
            val typingEvent: LoopLinkEvent = TypingEvent(
                roomId = roomId,
                userId = currentUserId,
                isTyping = isTyping
            )

            // Encode using the parent interface
            val eventJson = AppJson.encodeToString<LoopLinkEvent>(typingEvent)
            chatRepository.sendMessage(roomId, eventJson)
        }
    }

    // ADDED: A debounced version for the UI to call
    fun sendTypingEventDebounced(roomId: Int) {
        // Cancel any pending "stop" job
        stopTypingJobs[roomId]?.cancel()

        // Send the "is typing" event immediately
        sendTypingEvent(roomId, true)

        // Launch a new job that will send "stop typing" after a delay
        stopTypingJobs[roomId] = viewModelScope.launch {
            delay(2000L) // 2-second window
            sendTypingEvent(roomId, false)
        }
    }

    fun setActiveRoom(roomId: Int?) {
        println("Chat View Model: Setting active room to $roomId")
        _activeRoomId.value = roomId
    }

    fun onMessageReceived(roomId: Int) {
        if (roomId != _activeRoomId.value) {
            updateRoomProperty(roomId) {
                it.copy(unread = it.unread + 1)
            }
            println("Incrementing value of $roomId unread message to ${_rooms.value[roomId]?.unread}")
        }
    }

    fun clearUnreadCount(roomId: Int) {
        updateRoomProperty(roomId) {
            it.copy(unread = 0)
        }
        println("Clearing unread messages for $roomId")
    }

    val roomsWithStatus: StateFlow<List<RoomItem>> =
        combine(_rooms,
            chatRepository.activeSessions,
            chatRepository.store.stateFlow
        ) { rooms, sessions, storeState ->

            val roomsWithStatus = rooms.values.map { room ->
                if (sessions.containsKey(room.id)) {
                    room.copy(status = ConnectionStatus.Connected)
                } else {
                    room
                }
            }

            roomsWithStatus.sortedByDescending { room ->
                storeState.rooms[room.id]?.lastOrNull()?.seconds ?: room.id.toLong()
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

    fun addRoomPfpPath(roomId: Int, pfpPath: String) {
        _rooms.update { curRooms ->
            if (!curRooms.containsKey(roomId)) return@update curRooms

            val room = curRooms[roomId]!!
            if (room.pfpPath == null) curRooms + (roomId to (room.copy(pfpPath = pfpPath)))
            else curRooms
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
        updateRoomProperty(roomId) { it.copy(status = connectionStatus) }
    }

    fun roomExists(roomId: Int): Boolean = _rooms.value.containsKey(roomId)


    fun updateRoomTheme(roomId: Int, newTheme: ChatTheme) {
        updateRoomProperty(roomId) { it.copy(chatTheme = newTheme) }
    }

    fun getRoomTheme(roomId: Int): ChatTheme? = _rooms.value[roomId]?.chatTheme
    fun getRoomLabel(roomId: Int): String? = _rooms.value[roomId]?.label
    fun getRoomStatus(roomId: Int): ConnectionStatus? = _rooms.value[roomId]?.status

    private val _downloadedFileIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedFileIds = _downloadedFileIds.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()
    fun getRoom(roomId: Int): RoomItem? = _rooms.value[roomId]
    fun getPfp(roomId: Int): String? =
        _rooms.value[roomId]?.pfpPath

    fun isGroup(roomId: Int): Boolean = _rooms.value[roomId]?.isGroup ?: false

    fun getPeerDefaultColor(roomId: Int): Color =
        _rooms.value[roomId]?.chatTheme?.defaultPeerColor!!

    fun lastMessageFor(roomId: Int): StateFlow<Message?> {
        return chatRepository.getLastMessage(roomId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun setBackgroundImage(roomId: Int, newSourcePath: String) {
        val fileRepository: FileRepository = get(FileRepository::class.java)
        println("Got: $newSourcePath")
        viewModelScope.launch {
            val newManagedFile = fileRepository.copyFileToInternalStorage(newSourcePath)
            println(newManagedFile?.fileId)
            val currentTheme = getRoomTheme(roomId) ?: ChatTheme.default()
            val currentBackgroundImage = currentTheme.backgroundImagePath

            if (currentBackgroundImage != null && currentBackgroundImage != newManagedFile?.fileId) {
                null
            }
            val internalPath = fileRepository.getFileInternalPath(fileId = newManagedFile!!.fileId)
            val updataedTheme = currentTheme.copy(
                backgroundImagePath = internalPath
            )
            println("New theme back: ${updataedTheme.backgroundImagePath}")
            updateRoomTheme(roomId, updataedTheme)

//            cleanupOrphanFiles()
        }
    }

    fun sendMessage(roomId: Int, text: String, attachedFile: String? = null){
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = userRepository.getUserIdAndName().first ?: return@launch
            println("Got message from $roomId with Text: $text")
            if(attachedFile != null){
                val managedFile = fileRepository.copyFileToInternalStorage(attachedFile)
                if(managedFile == null){
                    println("ChatViewModel: Failed to process shared file, returned null")
                    return@launch
                }

                _downloadedFileIds.update { it +  managedFile.fileId}

                val fileMessage = Message(
                    userId = currentUserId,
                    roomId = roomId,
                    fileInfo = managedFile,
                    text = text.ifBlank { null }
                )
                chatRepository.store.send(Action.SendMessage(roomId, fileMessage))
                val messageJson = AppJson.encodeToString<LoopLinkEvent>(fileMessage)
                chatRepository.sendMessage(roomId, messageJson)
            } else if(text.isNotEmpty()){
                val textMessage = Message(
                    currentUserId,
                    roomId,
                    text
                )
                chatRepository.store.send(Action.SendMessage(roomId, textMessage))
                val messageJson = AppJson.encodeToString<LoopLinkEvent>(textMessage)
                println("Passing text to ChatRepository")
                chatRepository.sendMessage(roomId, messageJson)
            }

        }
    }

    fun onDownloadFile(message: Message) {
        val fileInfo = message.fileInfo ?: return
        val fileId = fileInfo.fileId

        if (_downloadedFileIds.value.contains(fileId)) {
            println("File already downloaded.")
            return
        }

        val sender: User? = userRepository.getUserById(message.userId)

        val host = sender?.hostAddress
        val port = sender?.port

        if (host == null || port == null) {
            println("Error: Cannot download file. Peer network info is missing.")
            return
        }

        println("Downloading file ${fileInfo.originalFileName} from http://$host:$port/files/$fileId")

        viewModelScope.launch(Dispatchers.IO){
            try{
                val client = createKtorClient()
                val outputFile = File(fileRepository.getDirectory(DIRECTORIES.FilesDir), fileId)


                client.prepareGet("http://$host:$port/files/$fileId"){
                    onDownload {
                        bytesSentTotal, contentLength ->
                        val progress = if (contentLength != null && contentLength > 0) {
                            bytesSentTotal.toFloat() / contentLength.toFloat()
                        } else {
                            0f
                        }

                        _downloadProgress.update { currentProgress ->
                            currentProgress + (fileId to progress)
                        }
                    }
                }.execute{ httpResponse ->
                    val channel: ByteReadChannel = httpResponse.body()
                    outputFile.outputStream().use { fileOutputStream ->
                        while(!channel.isClosedForRead){
                            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                            while(!packet.exhausted()){
                                val bytes = packet.readByteArray()
                                fileOutputStream.write(bytes)
                            }
                        }
                    }
                }

                println("File downloaded successfully: ${fileInfo.originalFileName}")
                _downloadedFileIds.update { it + fileId }
                _downloadProgress.update { it - fileId }

            } catch (e: Exception){
                println("Cannot download file: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun cleanupOrphanFiles() {
        TODO()
    }
}