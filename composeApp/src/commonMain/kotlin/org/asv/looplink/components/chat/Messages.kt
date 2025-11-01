package org.asv.looplink.components.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.asv.looplink.components.painterFromFile
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

@Composable
fun DateSeparatorChip(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .background(Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(text, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalTime::class)
fun areOnSameDay(seconds1: Long, seconds2: Long): Boolean {
    val zone = TimeZone.currentSystemDefault()
    val date1 = Instant.fromEpochSeconds(seconds1).toLocalDateTime(zone).date
    val date2 = Instant.fromEpochSeconds(seconds2).toLocalDateTime(zone).date
    return date1 == date2
}
