package org.asv.looplink.components.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.compose.koinInject

@Composable
internal actual fun Messages(
    modifier: Modifier,
    roomId: Int,
    isWideScreen: Boolean,
    messages: List<Message>
) {
    val listState = rememberLazyListState()
    val userRespository: UserRespository = koinInject()
//    var lastUserId: String? = null
    val chatViewModel: ChatViewModel = koinInject()
    val currentUserId = userRespository.getUserIdAndName().first
    val chatTheme = chatViewModel.getRoomTheme(roomId) ?: ChatTheme.default()

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
                .padding(start = 10.dp, end = 10.dp),
            state = listState,
        ) {

            item { Spacer(Modifier.size(20.dp)) }
            itemsIndexed(messages, key = {_, message -> message.id }) { index, message ->
                val prev = messages.getOrNull(index - 1)
                val showElements = prev == null || prev.userId != message.userId
                ChatMessage(
                    isMyMessage = message.userId == currentUserId,
                    roomId, message, !showElements
                )
            }
            item { Box(Modifier.height(10.dp)) }

        }
    }
}