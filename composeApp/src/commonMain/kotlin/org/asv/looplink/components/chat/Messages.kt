package org.asv.looplink.components.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.asv.looplink.components.painterFromFile
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject

@Composable
internal expect fun Messages(
    modifier: Modifier = Modifier,
    roomId: Int,
    isWideScreen: Boolean,
    messages: List<Message>
)


@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserPic(userId: String, roomId: Int) {
    val chatViewModel: ChatViewModel = koinInject()
    val userRepository: UserRepository = koinInject()
    val imageSize = 48f
    val pfpPath = userRepository.getUserpfpPath(userId = userId)
    val userColor = chatViewModel.getPeerDefaultColor(roomId)
    val painter = pfpPath?.let {
        painterFromFile(pfpPath)
    } ?: object : Painter() {
        override val intrinsicSize: Size = Size(imageSize, imageSize)
        override fun DrawScope.onDraw() {
            drawRect(userColor, size = Size(imageSize * 4, imageSize * 4))
        }
    }

    Image(
        modifier = Modifier
            .size(imageSize.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = "User picture"
    )
}
