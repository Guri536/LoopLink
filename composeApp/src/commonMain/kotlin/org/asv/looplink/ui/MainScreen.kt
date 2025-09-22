package org.asv.looplink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.asv.looplink.PlatformType
import org.asv.looplink.components.LocalTabNavigator
import org.asv.looplink.components.SettingsPage
import org.asv.looplink.components.chat.ChatAppWithScaffold
import org.asv.looplink.getPlatformType

data class RoomItem(val id: String, val label: String, val unread: Int = 0)
data class TopTab(val id: String, val label: String)

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val showSideScreen = getPlatformType() == PlatformType.DESKTOP
        TabNavigator(EmptyChatTab) { tabNavigator ->
            CompositionLocalProvider(
                LocalTabNavigator provides tabNavigator
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Sidebar(
                        modifier = Modifier
                            .fillMaxWidth(if (showSideScreen) 0.15f else 1f)
                            .fillMaxHeight(),
                        rooms = listOf(
                            RoomItem(
                                id = "1",
                                label = "Room 1",
                                unread = 5
                            ),
                            RoomItem(
                                id = "2",
                                label = "Room 2",
                                unread = 0
                            )
                        ),
                        onRoomClick = { room ->
                            tabNavigator.current = ChatTab(room)
                        },
                        onProfileClick = {},
                        onSettingsClick = {
                            navigator.push(SettingsPage())
                        }
                    )

                    if (showSideScreen) {
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxSize()
                                .background(Color(0xFF1C1C1C))
                        ) {
                            CurrentTab()
                        }
                    }
                }
            }


//            Box(
//                modifier = Modifier
//                    .width(28.dp)
//                    .fillMaxHeight()
//                    .background(Color(0xFFECE0E4))
//            )
        }
    }
}

@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    rooms: List<RoomItem>,
    onRoomClick: (RoomItem) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Chats",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
            ) {
                items(rooms) { room ->
                    SidebarRoomItem(room = room, onClick = { onRoomClick(room) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement =
                Arrangement.spacedBy(8.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(.8f)
            ) {
                Text("Profile")
            }
            Button(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(.8f)
            ) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun SidebarRoomItem(room: RoomItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 65.dp)
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF).copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFF7A8A8), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(room.id.take(2), color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(room.label, color = Color.White)
            if (room.unread > 0) {
                Text(
                    "${room.unread} unread",
                    color = Color(0xFFFFB4A2),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "Chat icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select a chat to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

object EmptyChatTab : Tab {
    private fun readResolve(): Any = EmptyChatTab
    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 0u, title = "Empty Chat"
            )
        }

    @Composable
    override fun Content() {
        EmptyChatPlaceholder()
    }
}

data class ChatTab(val room: RoomItem) : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = room.label
            val index = remember(room.id) { room.id.hashCode().toUShort() }
            return remember { TabOptions(index, title) }
        }

    @Composable
    override fun Content() {
        ChatAppWithScaffold(true, room)
    }
}