package org.asv.looplink.components.chat

//import androidx.compose.material.TopAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.asv.looplink.components.LocalTabNavigator
import org.asv.looplink.ui.EmptyChatTab
import org.asv.looplink.ui.RoomItem
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ui.theme.AppTheme

val myUser = User("Me", picture = null)
val store = CoroutineScope(SupervisorJob()).createStore()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppWithScaffold(
    displayTextField: Boolean = true,
    room: RoomItem
) {
    val tabNavigator = LocalTabNavigator.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    AppTheme {
        Scaffold(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent {
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                        if (focusRequester.freeFocus()) {
                            tabNavigator?.current = EmptyChatTab
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
                        IconButton(onClick = { tabNavigator?.current = EmptyChatTab }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }) { contentPadding ->
            ChatApp(
                displayTextField = displayTextField,
                modifier = Modifier.padding(contentPadding)
                    .background(MaterialTheme.colorScheme.background.copy(.9f)),
                room = room
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
    room: RoomItem
) {
    val state by store.stateFlow.collectAsState()

    repeat(10) {
        store.send(Action.SendMessage(room.id, Message(myUser, "Hello")))
        store.send(
            Action.SendMessage(
                room.id, Message(
                    User(
                        "Some",
                        ColorProvider.getColor(),
                        null
                    ), "Hello"
                )
            )
        )
    }

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
                            ) {
                                text ->
                                store.send(
                                    Action.SendMessage(
                                        roomId = room.id,
                                        message = Message(myUser, text)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
//        var lastFriend = friends.random()
//        var lastMessage = friendMessages.random()
//        while (true) {
//            val thisFriend = friends.random()
//            val thisMessage = friendMessages.random()
//            if (thisFriend == lastFriend) continue
//            if (thisMessage == lastMessage) continue
//            lastFriend = thisFriend
//            lastMessage = thisMessage
//            store.send(
//                Action.SendMessage(
//                    message = Message(
//                        user = thisFriend,
//                        text = thisMessage
//                    )
//                )
//            )
//            delay(5000)
//        }
    }
}
