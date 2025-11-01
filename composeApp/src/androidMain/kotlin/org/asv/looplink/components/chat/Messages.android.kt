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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.ui.rememberFormattedDate
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
internal actual fun Messages(
    modifier: Modifier,
    roomId: Int,
    isWideScreen: Boolean,
    messages: List<Message>
) {
    val listState = rememberLazyListState()
    val userRepository: UserRepository = koinInject()
    val chatViewModel: ChatViewModel = koinInject()
    val currentUserId = userRepository.getUserIdAndName().first

    val showNameOfPeer = !chatViewModel.isGroup(roomId)

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
            messages.forEachIndexed { index, message ->
                val prevMessage = messages.getOrNull(index - 1)
                val showDateChip = prevMessage == null || !areOnSameDay(prevMessage.seconds, message.seconds)

                if (showDateChip) {
                    item(key = "date_chip_${message.id}") {
                        val formattedDate = rememberFormattedDate(message.seconds)
                        DateSeparatorChip(formattedDate)
                    }
                }

                item(key = message.id) {
                    val showElements = prevMessage == null || prevMessage.userId != message.userId
                    ChatMessage(
                        isMyMessage = message.userId == currentUserId,
                        roomId, message, !showElements,
                        showNameOfPeer
                    )
                }
            }
            item { Box(Modifier.height(10.dp)) }

        }
    }
}