package org.asv.looplink.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.asv.looplink.PlatformType
import org.asv.looplink.components.LocalAppNavigator
import org.asv.looplink.components.painterFromFile
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.getPlatformType
import org.asv.looplink.operations.formatBytes
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.ConnectionStatus
import org.asv.looplink.ui.FallbackAvatar
import org.asv.looplink.ui.FilePicker
import org.asv.looplink.ui.FilePickerMode
import org.asv.looplink.ui.RoomItem
import org.asv.looplink.ui.TabType
import org.asv.looplink.ui.angledLinearGradientBrush
import org.asv.looplink.ui.backdropBlur
import org.asv.looplink.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import ui.theme.AppTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, InternalVoyagerApi::class)
@Composable
fun ChatAppWithScaffold(
    displayTextField: Boolean = true,
    roomId: Int,
) {
    val navigator = LocalAppNavigator.currentOrThrow
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val chatViewModel: ChatViewModel = koinInject()

    val rooms by chatViewModel.roomsWithStatus.collectAsStateWithLifecycle()
    val room by remember(rooms, roomId) {
        derivedStateOf { rooms.find { it.id == roomId } }
    }

    val chatTheme = room?.chatTheme ?: ChatTheme.default()
    val chatLabel = room?.label ?: "Chat"
    val isGroup = room?.isGroup ?: false
    val tabs = room?.groupDetails?.tabs ?: emptyList()
    var selectedTabIndex by remember(roomId) { mutableStateOf(0) }

    val chatAppBackgroundModifier: Modifier =
        if (chatTheme.backgroundImagePath != null) {
            Modifier.background(Color.Transparent)
        } else if (chatTheme.backgroundBrush != null) {
            Modifier.background(chatTheme.backgroundBrush)
        } else {
            Modifier.background(chatTheme.backgroundColor)
        }

    val showFilePicker = remember { mutableStateOf(false) }

    val chatRepository: ChatRepository = koinInject()

    val isRoomDetailsVisible by chatViewModel.isRoomDetailsVisible.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "Blinking")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkAlpha"
    )
    val indicatorAlpha = if (room?.status == ConnectionStatus.Connecting) blinkAlpha else 1f

    val activeSessionColor: Color = when (room?.status) {
        ConnectionStatus.Idle -> Color.Gray
        ConnectionStatus.Connected -> Color.Green
        ConnectionStatus.Connecting -> Color.Green
        is ConnectionStatus.Error -> Color.Red
        else -> Color.Gray
    }

    DisposableEffect(Unit) {
        println("ChatApp: Screen created")
        onDispose {
            println("ChatApp: Screen disposed")
        }
    }

    AppTheme {
        Scaffold(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent {
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                        if (isRoomDetailsVisible) {
                            chatViewModel.setRoomDetailsVisible(false)
                        } else if (focusRequester.freeFocus()) {
                            navigator.pop()
                        } else {
                            focusManager.clearFocus()
                            focusRequester.requestFocus()
                        }
                        true
                    } else {
                        false
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        focusRequester.requestFocus()
                    }
                },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.clickable(
//                                enabled = isGroup,
                                onClick = { chatViewModel.setRoomDetailsVisible(true) }
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF7A8A8), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val commonModifier = Modifier.fillMaxSize()
                                val path = room?.customPfpPath ?: room?.pfpPath
                                if (path != null) {
                                    AsyncImage(
                                        model = File(path), // Pass a File object or null
                                        contentDescription = "Room Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = commonModifier.clip(CircleShape)
                                    )
                                } else {
                                    FallbackAvatar(room?.label ?: "Unknown", commonModifier)
                                }

                                room?.isGroup?.let {
                                    if (!it) {
                                        Box(
                                            modifier = Modifier.size(12.dp)
                                                .graphicsLayer(alpha = indicatorAlpha)
                                                .background(activeSessionColor, shape = CircleShape)
                                                .align(Alignment.BottomStart)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                chatLabel,
                                color = chatTheme.topBarTextColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = chatTheme.topBarColor
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.pop()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = chatTheme.topBarTextColor
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            chatViewModel.setRoomDetailsVisible(true)

                        }) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Customize",
                                tint = chatTheme.topBarTextColor
                            )
                        }
                    }
                )
            }) { contentPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(contentPadding))
            {
                FilePicker(showFilePicker.value) { it ->
                    showFilePicker.value = false
                    if (it != null) chatViewModel.setBackgroundImage(roomId, it)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (isGroup && tabs.isNotEmpty()) {
                        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = {
                                        selectedTabIndex = index
                                        // Load content for the selected tab
                                        chatViewModel.loadTabContent(roomId, tab.type)
                                    },
                                    text = { Text(tab.label) }
                                )
                            }
                        }
                    }

                    // --- Content Switch ---
                    Box(modifier = Modifier.weight(1f)) {
                        val selectedTabType = tabs.getOrNull(selectedTabIndex)?.type

                        if (!isGroup) {
                            // Default 1-on-1 Chat
                            ChatApp(
                                displayTextField = displayTextField,
                                modifier = chatAppBackgroundModifier,
                                roomId = roomId
                            )
                        } else {
                            // Group Chat with Tabs
                            when (selectedTabType) {
                                TabType.CHAT -> ChatApp(
                                    displayTextField = true,
                                    modifier = chatAppBackgroundModifier,
                                    roomId = roomId
                                )

                                TabType.FILES -> GroupFilesTab(
                                    roomId = roomId,
                                    modifier = chatAppBackgroundModifier,
                                    chatViewModel = chatViewModel
                                )

                                TabType.PANELS -> GroupPanelsTab(
                                    roomId = roomId,
                                    modifier = chatAppBackgroundModifier,
                                    chatViewModel = chatViewModel
                                )

                                TabType.ASSIGNMENTS -> GroupPanelsTab(
                                    roomId,
                                    chatAppBackgroundModifier,
                                    chatViewModel
                                )

                                else -> {
                                    // Fallback for unhandled or null tab types
                                    ChatApp(
                                        displayTextField = true,
                                        modifier = chatAppBackgroundModifier,
                                        roomId = roomId
                                    )
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(
    modifier: Modifier = Modifier,
    displayTextField: Boolean = true,
    roomId: Int,
) {
    val chatRepository: ChatRepository = koinInject()
    val state by chatRepository.store.stateFlow.collectAsStateWithLifecycle()
    val chatViewModel: ChatViewModel = koinInject()

    val isWideScreen = getPlatformType() == PlatformType.DESKTOP

    val rooms by chatViewModel.roomsWithStatus.collectAsStateWithLifecycle()
    val room = remember(rooms, roomId) {
        rooms.find { it.id == roomId }
    }
    val chatTheme = room?.chatTheme ?: ChatTheme.default()
    val test = chatRepository.activeSessions.collectAsStateWithLifecycle().value[roomId]
    LaunchedEffect(Unit) {
        println("ChatApp: Total Sessions in this chat: $test")
    }

    val isRoomDetailsVisible by chatViewModel.isRoomDetailsVisible.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        println("ChatApp: Screen created")
        onDispose {
            println("ChatApp: Screen disposed")
            chatViewModel.setRoomDetailsVisible(false)
        }
    }

    AppTheme {
        Surface(
            modifier = Modifier.background(Color.Transparent)
        ) {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                val backgroundImagePainter =
                    chatTheme.backgroundImagePath?.let { path ->
                        painterFromFile(path)
                    }

                if (backgroundImagePainter != null) {
                    Image(
                        painter = backgroundImagePainter,
                        contentDescription = "Background Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                    ) {
                        Messages(
                            modifier = Modifier
                                .padding(bottom = 70.dp),
                            roomId,
                            isWideScreen,
                            state.rooms[roomId].orEmpty()
                        )
                        if (displayTextField) {
                            SendMessage(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter),
                                roomId
                            )
                        }

                    }
                }
                AnimatedVisibility(
                    visible = isRoomDetailsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    room?.let {


                        RoomDetailsOverlay(
                            room = it,
                            onClose = { chatViewModel.setRoomDetailsVisible(false) },
                            onPfpChange = { imagePath ->
                                chatViewModel.updateRoomCustomPfp(roomId, imagePath)
                            }
                        )

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsOverlay(
    room: RoomItem,
    onClose: () -> Unit,
    onPfpChange: (String) -> Unit
) {
    println("Panel Opened")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Members", "Customize", "Files")
    val showPfpPicker = remember { mutableStateOf(false) }
    val chatViewModel: ChatViewModel = koinInject()

    var showProfile = remember { mutableStateOf(true) }
    val profilePicSize: Dp = remember(showProfile.value) {
        if (showProfile.value) 120.dp else 20.dp
    }

    FilePicker(showPfpPicker.value, FilePickerMode.MEDIA_ONLY) {
        showPfpPicker.value = false
        it?.let { onPfpChange(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onClose)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .backdropBlur(radius = 16.dp) // Our custom expect modifier
                .clickable(onClick = onClose)
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(0.8f)
                .align(Alignment.TopCenter)
                .clickable(enabled = false, onClick = {}) // Consume clicks
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Header with Image and Close Button ---
                if (showProfile.value) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Room PFP
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color(0xFFF7A8A8), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val commonModifier = Modifier.fillMaxSize()
                            val path = room.customPfpPath ?: room.pfpPath
                            if (path != null) {
                                AsyncImage(
                                    model = File(path), // Pass a File object or null
                                    contentDescription = "Room Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = commonModifier.clip(CircleShape)
                                )
                            } else {
                                FallbackAvatar(room.label, commonModifier)
                            }

                            if (room.customPfpPath != null && showProfile.value) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .align(Alignment.TopEnd)
                                ) {
                                    IconButton(
                                        onClick = {
                                            chatViewModel.removeCustomRoomPfp(room.id)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove Custom Pfp",
                                        )
                                    }
                                }
                            }
                            if (showProfile.value) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    IconButton(
                                        onClick = {
                                            showPfpPicker.value = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Change Picture",
                                        )
                                    }
                                }
                            }
                        }
                        // Close Button
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // --- Room Info ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = room.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center).padding(top = 8.dp)
                    )
                    if (!showProfile.value) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                if (room.isGroup) {
                    Text(
                        text = room.groupDetails?.description ?: "No description provided.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- Overlay Tabs ---
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    when (selectedTabIndex) {
                        0 -> showProfile.value = true
                        else -> showProfile.value = false
                    }
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                    when (index) {
                                        0 -> Icons.Default.People
                                        1 -> Icons.Default.Settings
                                        2 -> Icons.Default.FileCopy
                                        else -> Icons.Default.QuestionMark
                                    },
                                    contentDescription = title
                                )
                            }
                        )
                    }
                }

                // --- Overlay Tab Content ---
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> MembersListTab(members = room.members)
                        1 -> RoomCustomizationTab(room = room)
                        2 -> GroupFilesTab(roomId = room.id)
                    }
                }
            }
        }
    }
}

@Composable
fun MembersListTab(members: List<String>, userRepository: UserRepository = koinInject()) {
    val users by userRepository.knownUsers.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(members) { memberId ->
            val user = users[memberId]
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF7A8A8), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val commonModifier = Modifier.fillMaxSize()
                    val path = user?.pfpPath
                    if (path != null) {
                        AsyncImage(
                            model = File(path), // Pass a File object or null
                            contentDescription = "Room Image",
                            contentScale = ContentScale.Crop,
                            modifier = commonModifier.clip(CircleShape)
                        )
                    } else {
                        FallbackAvatar(user?.name ?: "Unknown", commonModifier)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(user?.name ?: "Unknown User", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun RoomCustomizationTab(
    room: RoomItem,
    chatViewModel: ChatViewModel = koinInject()
) {
    var tempTheme by remember(room.chatTheme) { mutableStateOf(room.chatTheme) }
    val showImagePicker = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val colorSwatches = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
        Color(0xFFF5F5F5), // Light Grey

        // Calm Pastels
        Color(0xFFD3E3FD), // Light Blue
        Color(0xFFC9E6C4), // Light Green
        Color(0xFFFDEBC8), // Light Peach
        Color(0xFFFFDDE1), // Light Pink
        Color(0xFFE6E0F8), // Light Lavender

        // Muted Tones
        Color(0xFFA0C4FF), // Soft Sky Blue
        Color(0xFFB2DFDB), // Muted Mint
        Color(0xFFD7CCC8),  // Light Taupe

        // Dark Tones //
        Color(0xFF37474F), // Dark Slate Grey
        Color(0xFF455A64), // Blue Grey
        Color(0xFF2C3E50), // Dark Slate Blue
        Color(0xFF4E342E), // Dark Brown
        Color(0xFF3E2723), // Deepest Brown
        Color(0xFF263238)  // Very Dark Blue Grey
    )

    val gradientSwatches = listOf(
        listOf(Color(0xFFa1c4fd), Color(0xFFc2e9fb)), // Light Blue
        listOf(Color(0xFFff9a9e), Color(0xFFfecfef)), // Soft Pink
        listOf(Color(0xFFB2DFDB), Color(0xFF80CBC4)), // "Ocean" Mint/Teal
        listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)), // "Dusk" Light Lilac
        listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80)), // "Sunrise" Soft Apricot
        listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4)), // "Sand" Taupe/Beige
        listOf(
            Color(0xFFa1c4fd), // Light Blue
            Color(0xFFE6E0F8), // Light Lavender
            Color(0xFFff9a9e)  // Soft Pink
        ), // "Dreamy Sky"

        listOf(
            Color(0xFFC9E6C4), // Light Green
            Color(0xFFFDEBC8), // Light Peach
            Color(0xFFE1BEE7)  // Light Lilac
        ), // "Meadow"

        listOf(
            Color(0xFFD7CCC8), // Light Taupe
            Color(0xFFB2DFDB), // Muted Mint
            Color(0xFFA0C4FF)  // Soft Sky Blue
        ), // "Seaside"
        listOf(
            Color(0xFFA0C4FF), // Soft Sky Blue
            Color(0xFFB2DFDB), // Muted Mint
            Color(0xFFFDEBC8), // Light Peach
            Color(0xFFE6E0F8)  // Light Lavender
        ),  // "Pastel Horizon"
        // --- Dark & Moody Gradients (NEW) ---
        listOf(
            Color(0xFF2C3E50), // Dark Slate Blue
            Color(0xFF4A688A)  // Muted Blue
        ), // "Midnight"

        listOf(
            Color(0xFF34495E), // Wet Asphalt
            Color(0xFF2C3E50)  // Dark Slate Blue
        ), // "Deep Space"

        listOf(
            Color(0xFF232526), // Almost Black
            Color(0xFF414345)  // Dark Grey
        ), // "Ashes"

        listOf(
            Color(0xFF0F2027), // Deepest Blue
            Color(0xFF203A43), // Dark Teal
            Color(0xFF2C5364)  // Muted Navy
        ), // "Deep Ocean"

        listOf(
            Color(0xFF4E342E), // Dark Brown
            Color(0xFF3E2723), // Deepest Brown
            Color(0xFF263238)  // Very Dark Blue Grey
        ), // "Forest Night"

        listOf(
            Color(0xFF455A64), // Blue Grey
            Color(0xFF37474F), // Dark Slate Grey
            Color(0xFF2C3E50), // Dark Slate Blue
            Color(0xFF263238)  // Very Dark Blue Grey
        ) // "Storm"
    )

    val onThemeChange: (ChatTheme.() -> ChatTheme) -> Unit = {
        tempTheme = it(tempTheme)
    }

    FilePicker(showImagePicker.value, FilePickerMode.MEDIA_ONLY) { imagePath ->
        showImagePicker.value = false
        if (imagePath != null) {
            onThemeChange {
                copy(
                    backgroundImagePath = imagePath,
                    // Clear other background types
                    backGroundColorArgb = Color.Black.toArgb(), // Default fallback
                    backgroundGradientArgb = null
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Padding is now on the parent
        horizontalAlignment = Alignment.CenterHorizontally,
        // No verticalScroll or spacing here
    ) {

        // --- PART 1: STICKY TOP (Anchored Preview) ---
        Text("Live Preview", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SampleMessagePreview(
            theme = tempTheme,
            room.id,
            room.members.getOrElse(0) { "Peer" },
            room.members.getOrElse(1) { "User" },
            tempTheme.backgroundGradientAngle
        )
        Spacer(Modifier.height(16.dp)) // Space before the scrollable content

        // --- PART 2: SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .weight(1f) // <-- This makes the Column fill the available space
                .verticalScroll(rememberScrollState()), // <-- This makes this part scroll
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing is now here
        ) {
            HorizontalDivider()
            Text("Background", style = MaterialTheme.typography.titleMedium)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Solid Colors",
                    style = MaterialTheme.typography.labelLarge,
                )
                ColorSelectorRow(colorSwatches) { color ->
                    onThemeChange {
                        copy(
                            backGroundColorArgb = color.toArgb(),
                            backgroundGradientArgb = null,
                            backgroundImagePath = null
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Gradient", style = MaterialTheme.typography.labelLarge)
                GradientSelectorRow(gradientSwatches) { colors ->
                    onThemeChange {
                        copy(
                            backgroundGradientArgb = colors.ifEmpty { null }?.map { it.toArgb() },
                            backgroundImagePath = null
                        )
                    }
                }
            }

            AnimatedVisibility(visible = tempTheme.backgroundGradientArgb != null) {
                Column {
                    Text(
                        "Gradient Angle: ${tempTheme.backgroundGradientAngle?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = tempTheme.backgroundGradientAngle ?: 0f,
                        onValueChange = { angle ->
                            onThemeChange { copy(backgroundGradientAngle = angle) }
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = SliderDefaults.colors().copy(
                            thumbColor = Color(
                                tempTheme.backgroundGradientArgb?.get(0)
                                    ?: SliderDefaults.colors().thumbColor.toArgb()
                            ),
                            activeTrackColor = Color(
                                tempTheme.backgroundGradientArgb?.get(1)
                                    ?: SliderDefaults.colors().activeTrackColor.toArgb()
                            )
                        )
                    )
                }
            }

            Button(onClick = { showImagePicker.value = true }) {
                Text("Set Background Image")
            }
            if (tempTheme.backgroundImagePath != null) {
                TextButton(onClick = {
                    onThemeChange { copy(backgroundImagePath = null) }
                }) {
                    Text("Clear Image")
                }
            }

            HorizontalDivider()
            Text("Chat Bubbles", style = MaterialTheme.typography.titleMedium)

            // 3. The Color Pickers
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between pickers
            ) {
                ThemeColorPicker("My Message Color", colorSwatches) { color ->
                    onThemeChange { copy(myMessageBackgroundColorArgb = color.toArgb()) }
                }

                ThemeColorPicker("Peer Message Color", colorSwatches) { color ->
                    onThemeChange { copy(peerMessageBackgroundColorArgb = color.toArgb()) }
                }
            }

            HorizontalDivider()
            Text("Text Colors", style = MaterialTheme.typography.titleMedium)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between pickers
            ) {
                ThemeColorPicker("My Text Color", colorSwatches) { color ->
                    onThemeChange { copy(myTextColorArgb = color.toArgb()) }
                }
                ThemeColorPicker("Peer Text Color", colorSwatches) { color ->
                    onThemeChange { copy(peerTextColorArgb = color.toArgb()) }
                }
            }
            Spacer(Modifier.weight(1f)) // Push save button to bottom

            // 4. The Save Button
        }
        Button(
            onClick = {
                chatViewModel.updateRoomTheme(room.id, tempTheme)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}

@Composable
fun ThemeColorPicker(
    label: String,
    colorSwatches: List<Color>,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 130.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // This creates the "table" look
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 16.dp) // Ensure space between label and swatches
        )
        ColorSelectorRow(colorSwatches) { color ->
            onColorSelected(color)
        }
    }
}

@Composable
fun GradientSelectorRow(
    gradients: List<List<Color>>,
    onGradientSelected: (List<Color>) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "None" button
        item {
            TextButton(onClick = { onGradientSelected(emptyList()) }) {
                Text("None")
            }
        }

        items(gradients) { gradientColors ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)) // Apply gradient
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable { onGradientSelected(gradientColors) }
            )
        }
    }
}

@Composable
fun SampleMessagePreview(
    theme: ChatTheme,
    roomId: Int,
    peerId: String,
    userId: String,
    backgroundGradientAngle: Float?
) {

    val backgroundBrush = when {
        theme.backgroundImagePath != null -> SolidColor(Color.DarkGray) // Placeholder
        theme.backgroundGradientArgb != null -> Brush.angledLinearGradientBrush(
            colors = theme.backgroundGradientArgb.map { Color(it) },
            angle = backgroundGradientAngle ?: 0f
        )

        else -> SolidColor(Color(theme.backGroundColorArgb))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush) // Apply the correct brush
        ) {
            val backgroundImagePainter =
                theme.backgroundImagePath?.let { path ->
                    painterFromFile(path)
                }

            if (backgroundImagePainter != null) {
                Image(
                    painter = backgroundImagePainter,
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.35f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (theme.backgroundImagePath != null) {
                    Text(
                        "Image: ${theme.backgroundImagePath.takeLast(20)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .padding(bottom = 4.dp)
                    )
                }
                ChatMessage(
                    false,
                    roomId,
                    message = Message(
                        userId = peerId,
                        roomId = roomId,
                        text = "Peer Message"
                    ),
                    sameUser = false,
                    showNameOfPeer = false,
                    chatTheme = theme
                )
                ChatMessage(
                    true,
                    roomId,
                    message = Message(
                        userId = userId,
                        roomId = roomId,
                        text = "My Message"
                    ),
                    sameUser = false,
                    showNameOfPeer = true,
                    chatTheme = theme
                )
            }
        }
    }
}

@Composable
fun ColorSelectorRow(
    colors: List<Color>,
    onColorSelected: (Color) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
fun GroupFilesTab(
    roomId: Int,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinInject()
) {
    chatViewModel.loadTabContent(roomId, TabType.FILES)
    val files by chatViewModel.filesForRoom.collectAsStateWithLifecycle()

    // Date formatter
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LazyColumn(modifier = modifier.fillMaxSize().padding(8.dp)) {
        if (files.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No files have been shared in this group yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(files) { message ->
            message.fileInfo?.let { fileInfo ->
                FileListItem(
                    message = message,
                    dateFormatter = dateFormatter,
                    isDownloaded = true,
                    progress = null,
                    onDownload = {
                        chatViewModel.onDownloadFile(message)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@Composable
fun FileListItem(
    message: Message,
    dateFormatter: SimpleDateFormat,
    isDownloaded: Boolean,
    progress: Float?,
    onDownload: () -> Unit
) {
    val fileRepository: FileRepository = koinInject()
    val internalPath =
        fileRepository.getFileInternalPath(message.fileInfo!!.fileId, message.fileInfo.dir)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .clickable { fileRepository.openFileInDefaultApp(internalPath) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // File Icon and Details
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File Icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(
                    message.fileInfo?.originalFileName ?: "Unknown File",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${formatBytes(message.fileInfo?.sizeInBytes ?: 0)} - ${
                        dateFormatter.format(
                            Date(message.seconds * 1000)
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Download Button/Progress
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            if (progress != null && progress > 0f) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(40.dp)
                )
            } else if (!isDownloaded) {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download File"
                    )
                }
            }
            // If downloaded, show nothing (or a checkmark, for a future enhancement)
        }
    }
}


@Composable
fun GroupPanelsTab(
    roomId: Int,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = koinInject()
) {
    val announcements by chatViewModel.announcementsForRoom.collectAsStateWithLifecycle()

    // Date formatter
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LazyColumn(modifier = modifier.fillMaxSize().padding(8.dp)) {
        if (announcements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "There are no announcements for this group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(announcements) { message ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        message.text ?: "Empty Announcement",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        dateFormatter.format(Date(message.seconds * 1000)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
