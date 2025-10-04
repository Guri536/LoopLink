package org.asv.looplink.components.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal actual fun Messages(
    modifier: Modifier,
    messages: List<Message>
) {
    val listState = rememberLazyListState()
    var lastChat: Message? = null

    if (messages.isNotEmpty()) {
        LaunchedEffect(messages.last()) {
            listState.animateScrollToItem(messages.lastIndex, scrollOffset = 2)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 10.dp, end = 10.dp)
            ,
            state = listState,
        ) {
            item { Spacer(Modifier.size(20.dp)) }
            items(messages, key = { it.id }) {
                val space = lastChat?.user?.name == it.user.name
                lastChat = it
                ChatMessage(isMyMessage = it.user == myUser, it, space)
            }
            item { Box(Modifier.height(10.dp)) }

        }
    }
}