package org.asv.looplink.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpMethod
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
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
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import org.asv.looplink.DatabaseManager
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.GroupInviteEvent
import org.asv.looplink.components.chat.LoopLinkEvent
import org.asv.looplink.components.chat.Message
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
import org.asv.looplink.ui.GroupStructure
import org.asv.looplink.ui.GroupTabs
import org.asv.looplink.ui.GroupType
import org.asv.looplink.ui.RoomItem
import org.asv.looplink.ui.TabType
import org.koin.java.KoinJavaComponent.get
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val database: DatabaseManager
) : ViewModel() {
    private val _rooms = MutableStateFlow<Map<Int, RoomItem>>(emptyMap())
    val rooms = _rooms.asStateFlow()

    private val _activeRoomId = MutableStateFlow<Int?>(null)
    val activeRoomId = _activeRoomId.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<Int, Set<String>>>(emptyMap())
    val typingUsers = _typingUsers.asStateFlow()

    private val stopTypingJobs = mutableMapOf<Int, Job>()

    private val _filesForRoom = MutableStateFlow<List<Message>>(emptyList())
    val filesForRoom = _filesForRoom.asStateFlow()

    private val _announcementsForRoom = MutableStateFlow<List<Message>>(emptyList())
    val announcementsForRoom = _announcementsForRoom.asStateFlow()

    private val _isRoomDetailsVisible = MutableStateFlow(false)
    val isRoomDetailsVisible = _isRoomDetailsVisible.asStateFlow()

    init {
        println("ChatViewModel: init - Loading data from database...")
        viewModelScope.launch(Dispatchers.IO) {
            val roomsFromDb = database.getAllRooms()
            println("Getting Rooms: $roomsFromDb")
            _rooms.update { roomsFromDb.associateBy { it.id } }
            println("ChatViewModel: Loaded ${roomsFromDb.size} rooms.")

            val messagesFromDb = database.getAllMessages()
            messagesFromDb.forEach { (roomId, messages) ->
                if (messages.isNotEmpty()) {
                    messages.forEach { msg ->
                        chatRepository.store.send(Action.SendMessage(roomId, msg))
                    }
                }
            }
            println("ChatViewModel: Loaded messages for ${messagesFromDb.size} rooms.")
        }
    }

    fun setRoomDetailsVisible(isVisible: Boolean) {
        _isRoomDetailsVisible.value = isVisible
    }

    fun removeCustomRoomPfp(roomId: Int){
        _rooms.update { rooms ->
            if(!rooms.containsKey(roomId)) return@update rooms
            viewModelScope.launch {
                database.removeRoomCustomPfp(roomId)
            }
            rooms + (roomId to rooms[roomId]!!.copy(customPfpPath = null))
        }
    }

    fun loadTabContent(roomId: Int, tabType: TabType) {
        viewModelScope.launch(Dispatchers.IO) {
            when (tabType) {
                TabType.FILES -> {
                    val files = database.getFilesFromRoom(roomId)
                    _filesForRoom.update { files }
                }

                TabType.PANELS -> {
                    val announcements = database.getAnnouncementsFromRoom(roomId)
                    _announcementsForRoom.update { announcements }
                }

                TabType.CHAT -> {
                    // Chat messages are already loaded in the main store
                    // We can clear the others if we want
                    _filesForRoom.update { emptyList() }
                    _announcementsForRoom.update { emptyList() }
                }

                TabType.ASSIGNMENTS -> {
                    val assignmetns = database.getAnnouncementsFromRoom(roomId)
                    _announcementsForRoom.update { assignmetns }
                }

                else -> { /* Do nothing for other tab types for now */
                }
            }
        }
    }

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
        combine(
            _rooms,
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
                database.saveRoom(roomItem)
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
            viewModelScope.launch {
                database.updateRoomPfp(roomId, pfpPath)
            }

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
        viewModelScope.launch(Dispatchers.IO) {
            database.updateRoomTheme(roomId, newTheme)
            updateRoomProperty(roomId) { it.copy(chatTheme = newTheme) }
        }
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

    fun updateRoomCustomPfp(roomId: Int, sourceImagePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Copy image to internal storage
            val managedFile =
                fileRepository.copyFileToInternalStorage(sourceImagePath, DIRECTORIES.ConnDir)
            val internalPath = managedFile?.let {
                fileRepository.getFileInternalPath(it.fileId, DIRECTORIES.ConnDir)
            }

            if (internalPath == null) {
                println("ChatViewModel: Failed to copy custom PFP.")
                return@launch
            }

            // 2. Update the database
            database.updateRoomCustomPfp(roomId, internalPath)


            // 3. Update the in-memory state
            updateRoomProperty(roomId) {
                it.copy(customPfpPath = internalPath)
            }

            // TODO: We should probably clean up the old customPfpPath file
        }
    }

    fun sendMessage(roomId: Int, text: String, attachedFile: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = userRepository.getUserIdAndName().first ?: return@launch
            println("Got message from $roomId with Text: $text")

            val room = getRoom(roomId)
            val isHost = room != null && room.isGroup && room.groupDetails?.ownerId == currentUserId

            val message: Message

            if (attachedFile != null) {
                val managedFile = fileRepository.copyFileToInternalStorage(attachedFile)
                if (managedFile == null) {
                    println("ChatViewModel: Failed to process shared file, returned null")
                    return@launch
                }

                _downloadedFileIds.update { it + managedFile.fileId }

                message = Message(
                    userId = currentUserId,
                    roomId = roomId,
                    fileInfo = managedFile,
                    text = text.ifBlank { null }
                )
            } else if (text.isNotEmpty()) {
                message = Message(
                    currentUserId,
                    roomId,
                    text
                )
            } else {
                return@launch // Nothing to send
            }

            database.saveMessage(message)
            chatRepository.store.send(Action.SendMessage(roomId, message))
            val messageJson = AppJson.encodeToString<LoopLinkEvent>(message)
            chatRepository.sendMessage(roomId, messageJson)
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = createKtorClient()
                val outputFile = File(fileRepository.getDirectory(DIRECTORIES.FilesDir), fileId)


                client.prepareGet("http://$host:$port/files/$fileId") {
                    onDownload { bytesSentTotal, contentLength ->
                        val progress = if (contentLength != null && contentLength > 0) {
                            bytesSentTotal.toFloat() / contentLength.toFloat()
                        } else {
                            0f
                        }

                        _downloadProgress.update { currentProgress ->
                            currentProgress + (fileId to progress)
                        }
                    }
                }.execute { httpResponse ->
                    val channel: ByteReadChannel = httpResponse.body()
                    outputFile.outputStream().use { fileOutputStream ->
                        while (!channel.isClosedForRead) {
                            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                            while (!packet.exhausted()) {
                                val bytes = packet.readByteArray()
                                fileOutputStream.write(bytes)
                            }
                        }
                    }
                }

                println("File downloaded successfully: ${fileInfo.originalFileName}")
                _downloadedFileIds.update { it + fileId }
                _downloadProgress.update { it - fileId }

            } catch (e: Exception) {
                println("Cannot download file: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun cleanupOrphanFiles() {
        TODO()
    }

    @OptIn(ExperimentalTime::class)
    fun createGroup(
        groupName: String,
        selectedMembers: List<RoomItem>,
        description: String?,
        groupType: GroupType,
        tabs: List<GroupTabs> = listOf(GroupTabs(label = "Chat", type = TabType.CHAT))
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = userRepository.getUserIdAndName().first ?: return@launch

            // 1. Create a list of all member IDs (including the host)
            val memberIds = (selectedMembers.mapNotNull { room ->
                // Get the peer's ID from the 1-on-1 room
                room.members.firstOrNull { it != currentUserId }
            } + currentUserId).distinct()

            // 2. Generate a new, deterministic Room ID for the group
            val newRoomId = memberIds.sorted().joinToString().hashCode()

            // 3. Create the group's structure
            val groupDetails = GroupStructure(
                ownerId = currentUserId,
                description = description,
                creationTimeStamp = Clock.System.now().epochSeconds,
                groupType = groupType,
                tabs = tabs
            )

            // 4. Create the new RoomItem for the host
            val newGroupRoom = RoomItem(
                id = newRoomId,
                label = groupName,
                isGroup = true,
                groupDetails = groupDetails,
                members = memberIds,
                status = ConnectionStatus.Connected
            )

            // 5. Add the room to the host's UI immediately
            addRoom(newGroupRoom)

            // 6. Create the invite event
            val inviteEvent = GroupInviteEvent(
                roomId = newRoomId,
                groupName = groupName,
                groupDetails = groupDetails,
                memberIds = memberIds,
                hostId = currentUserId
            )

            val inviteJson = AppJson.encodeToString<LoopLinkEvent>(inviteEvent)

            // 7. Send the invite to all selected members via their 1-on-1 chat
            selectedMembers.forEach { memberRoom ->
                val peerRoomId = memberRoom.id // This is the 1-on-1 chat's ID
                chatRepository.sendMessage(peerRoomId, inviteJson)
            }
            println("ChatViewModel: Created group $groupName and sent invites.")
        }
    }

    /**
     * PHASE 3c (Client Logic): Called by the repository when a GroupInviteEvent is received.
     */
    fun onGroupInviteReceived(invite: GroupInviteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Check if we already have this room
            if (roomExists(invite.roomId)) {
                println("ChatViewModel: Received invite for group we're already in.")
                return@launch
            }

            // 2. Get host user from our repository (we should know them from a 1-on-1 chat)
            val hostUser = userRepository.getUserById(invite.hostId)
            if (hostUser?.hostAddress == null || hostUser.port == null) {
                println("Error: Received group invite from unknown host ${invite.hostId}. Cannot connect.")
                return@launch
            }

            // 3. Create the new RoomItem for the client
            val newGroupRoom = RoomItem(
                id = invite.roomId,
                label = invite.groupName,
                isGroup = true,
                groupDetails = invite.groupDetails,
                members = invite.memberIds,
                status = ConnectionStatus.Connecting // We'll connect right after
            )

            // 4. Add the room to the client's UI
            addRoom(newGroupRoom)

            // 5. --- AUTO-CONNECT TO HOST ---
            // This is the client's connection to the new group
            try {
                val localUserName = userRepository.getUserIdAndName().second
                val localUserUid = userRepository.getUserIdAndName().first ?: return@launch
                val localUserPort = userRepository.currentUserPort.value

                val encodedUID = URLEncoder.encode(localUserUid, StandardCharsets.UTF_8.toString())
                val encodedName =
                    URLEncoder.encode(localUserName, StandardCharsets.UTF_8.toString())
                val encodedPort = localUserPort.toString()

                val client = createKtorClient()
                println("ChatViewModel: Auto-connecting to group host ${hostUser.name} for room ${invite.roomId}")

                val session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = hostUser.hostAddress,
                    port = hostUser.port.toInt(),
                    // We use /initiate, but the server will need to be smart about it
                    path = "/looplink/initiate/${invite.roomId}?peerUid=$encodedUID&peerName=$encodedName&peerPort=$encodedPort"
                )

                // Add and listen to this new session
                chatRepository.addAndListenToClientSession(
                    invite.roomId,
                    session,
                    hostUser.hostAddress
                )

            } catch (e: Exception) {
                println("ChatViewModel: Auto-connect to group host failed: ${e.message}")
                updateRoomConnection(invite.roomId, ConnectionStatus.Error("Connection failed"))
            }
        }
    }
}