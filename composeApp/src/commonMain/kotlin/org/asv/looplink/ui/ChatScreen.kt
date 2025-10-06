package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.asv.looplink.components.chat.Action
import org.asv.looplink.components.chat.ChatAppWithScaffold
import org.asv.looplink.components.chat.Message
import org.asv.looplink.components.chat.store
import org.asv.looplink.network.discovery.ServiceInfo
import org.asv.looplink.viewmodel.RoomItem

class ChatScreen(
    private val serviceInfo: ServiceInfo,
    private val session: DefaultClientWebSocketSession
) : Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val roomId = remember { serviceInfo.hashCode() }

        DisposableEffect(session, Unit) {
            val incomingJob = session.incoming.consumeAsFlow()
                .onEach { frame ->
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        val message = Json.decodeFromString<Message>(receivedText)
                        store.send(Action.SendMessage(roomId = roomId, message = message))
                    }
                }
                .launchIn(scope)

            onDispose {
                incomingJob.cancel()
                scope.launch {
                    session.close()
                }
            }
        }

        ChatAppWithScaffold(
            room = RoomItem(roomId, serviceInfo.instanceName),
            session = session
        )
    }
}