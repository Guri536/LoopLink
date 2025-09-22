package org.asv.looplink.components.chat

//import androidx.compose.material.TopAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.asv.looplink.components.LocalTabNavigator
import org.asv.looplink.ui.EmptyChatTab
import org.asv.looplink.ui.RoomItem
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ui.theme.AppTheme

val myUser = User("Me", picture = null)
val friends = listOf(
    User("Alex", picture = null),
    User("Casey", picture = null),
    User("Sam", picture = null)
)
val friendMessages = listOf(
    "How's everybody doing today?",
    "I've been meaning to chat!",
    "When do we hang out next? 😋",
    "We really need to catch up!",
    "It's been too long!",
    "I can't\nbelieve\nit! 😱",
    "Did you see that ludicrous\ndisplay last night?",
    "We should meet up in person!",
    "How about a round of pinball?",
    "I'd love to:\n🍔 Eat something\n🎥 Watch a movie, maybe?\nWDYT?"
)
val store = CoroutineScope(SupervisorJob()).createStore()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppWithScaffold(
    displayTextField: Boolean = true,
    room: RoomItem
) {
    val tabNavigator = LocalTabNavigator.current
    AppTheme {
        Scaffold(
            topBar = {

                TopAppBar(
                    title = { Text(room.label) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
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
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatApp(
    modifier: Modifier = Modifier,
    displayTextField: Boolean = true,
) {
    val state by store.stateFlow.collectAsState()
    AppTheme {
        Surface {
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.weight(1f)) {
                        Messages(state.messages)
                    }
                    if (displayTextField) {
                        SendMessage { text ->
                            store.send(
                                Action.SendMessage(
                                    Message(myUser, text)
                                )
                            )
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
