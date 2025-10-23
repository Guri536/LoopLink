package org.asv.looplink.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.components.LocalAppNavigator
import org.asv.looplink.viewmodel.RoomItem
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ui.theme.AppTheme

val myUser = User("Me", picture = null)
val store = CoroutineScope(SupervisorJob()).createStore()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppWithScaffold(
    displayTextField: Boolean = true,
    room: RoomItem,
    session: DefaultWebSocketSession? = null
) {
    val navigator = LocalAppNavigator.currentOrThrow
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    DisposableEffect(session, Unit){
        val incomingJob = session?.incoming?.consumeAsFlow()
            ?.onEach { frame ->
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()
                    val message = Json.decodeFromString<Message>(receivedText)
                    store.send(Action.SendMessage(roomId = room.id, message = message))
                }
            }
            ?.launchIn(scope)

        onDispose {
            incomingJob?.cancel()
            scope.launch {
                session?.close()
            }
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
                    title = { Text(room.label) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(0.95f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.pop()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }) { contentPadding ->
            ChatApp(
                displayTextField = displayTextField,
                modifier = Modifier.padding(contentPadding)
                    .background(MaterialTheme.colorScheme.background.copy(.9f)),
                room = room,
                session = session
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(
    modifier: Modifier = Modifier,
    displayTextField: Boolean = true,
    room: RoomItem,
    session: DefaultWebSocketSession? = null
) {
    val state by store.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()

    AppTheme {
        Surface(
            modifier = Modifier
        ) {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
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
                            state.rooms[room.id].orEmpty()
                        )
                        if (displayTextField) {
                            SendMessage(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                            ) { text ->
                                val message = Message(myUser, text)
                                store.send(
                                    Action.SendMessage(
                                        roomId = room.id,
                                        message = message
                                    )
                                )
                                scope.launch {
                                    try {
//                                        println("Attempting to send message on session: ${session?.javaClass?.simpleName}")
                                        session?.send(Frame.Text(Json.encodeToString(message)))
//                                        println("Message sent successfully.")
                                    } catch (e: Exception) {
                                        println("Error sending message: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}