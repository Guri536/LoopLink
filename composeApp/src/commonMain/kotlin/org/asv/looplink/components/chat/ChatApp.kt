package org.asv.looplink.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.PlatformType
import org.asv.looplink.components.LocalAppNavigator
import org.asv.looplink.components.painterFromFile
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.getPlatformType
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.FilePicker
import org.asv.looplink.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, InternalVoyagerApi::class)
@Composable
fun ChatAppWithScaffold(
    displayTextField: Boolean = true,
    roomId: Int,
    session: DefaultWebSocketSession?
) {
    val navigator = LocalAppNavigator.currentOrThrow
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val chatViewModel: ChatViewModel = koinInject()
    val chatTheme = chatViewModel.getRoomTheme(roomId) ?: ChatTheme.default()
    val chatLabel = chatViewModel.getRoomLabel(roomId) ?: "Chat"

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
                        if (focusRequester.freeFocus()) {
                            navigator.pop()
                            true
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
                        Text(
                            chatLabel
//                            session.toString()
                            ,
                            color = chatTheme.topBarTextColor
                        )
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
                            showFilePicker.value = true
                        }) {
                            Icon(
                                Icons.AutoMirrored.Outlined.InsertDriveFile,
                                contentDescription = "Choose file",
                                tint = chatTheme.topBarTextColor
                            )
                        }
                        Spacer(Modifier.width(24.dp))
                        IconButton(
                            onClick = {
                                chatRepository.store.send(
                                    Action.SendMessage(
                                        roomId = roomId,
                                        message = Message(
                                            userId = "Unknown",
                                            roomId = 0,
                                            text = "Hey there"
                                        )
                                    )
                                )
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.Send,
                                contentDescription = "Send Test",
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

                ChatApp(
                    displayTextField = displayTextField,
                    modifier = chatAppBackgroundModifier,
                    roomId = roomId,
                    session = session
                )

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
    session: DefaultWebSocketSession?
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
    LaunchedEffect(Unit){
        println("ChatApp: Total Sessions in this chat: $test")
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
            }
        }
    }
}
