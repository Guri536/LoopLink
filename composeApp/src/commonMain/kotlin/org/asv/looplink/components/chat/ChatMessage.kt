package org.asv.looplink.components.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import org.asv.looplink.data.repository.FileRepository
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.isPlatformMobile
import org.asv.looplink.theme.ChatTheme
import org.asv.looplink.ui.VideoPlayer
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.compose.koinInject
import java.io.File

@Composable
fun Triangle(risingToTheRight: Boolean, background: Color) {
    Box(
        Modifier
//            .padding(bottom = 10.dp, start = 0.dp)
            .clip(TriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(6.dp)
    )
}

@Composable
fun ChatMessage(
    isMyMessage: Boolean,
    roomId: Int,
    message: Message,
    sameUser: Boolean = false,
    showNameOfPeer: Boolean = true,
    chatViewModel: ChatViewModel = koinInject(),
    chatTheme: ChatTheme = chatViewModel.getRoomTheme(roomId) ?: ChatTheme.default()
) {
    val userRepository: UserRepository = koinInject()
    val isMobiles = isPlatformMobile()

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(top = (if (sameUser) 2.dp else 8.dp)),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(
                    end = if (isMyMessage) 12.dp else 0.dp
                )
        ) {
            if (!isMyMessage) {
                if (sameUser) {
                    Spacer(Modifier.width(56.dp))
                } else {
                    Column {
                        UserPic(message.userId, message.roomId)
                    }
                    Spacer(Modifier.size(2.dp))
                    Column {
                        Triangle(true, chatTheme.peerMessageColor)
                    }
                }
            }

            Column {
                Box(
                    Modifier.clip(
                        RoundedCornerShape(
                            10.dp,
                            10.dp,
                            if (!isMyMessage) 10.dp else 0.dp,
                            if (!isMyMessage) 0.dp else 10.dp
                        )
                    )
                        .background(color = if (!isMyMessage) chatTheme.peerMessageColor else chatTheme.myMessageColor)
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                        .widthIn(max = if(isMobiles) 300.dp else 900.dp),
                ) {
                    Column {
                        if (!isMyMessage && !sameUser && !showNameOfPeer) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = userRepository.getUserName(message.userId) ?: "Unkown",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = chatTheme.defaultPeerColor,
                                    modifier = Modifier
                                )
                            }
                        }
                        Spacer(Modifier.size(3.dp))
                        when (message.type) {
                            MessageType.TEXT -> {
                                Text(
                                    text = message.text ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = if(isMobiles) 14.sp else 18.sp,
                                        letterSpacing = 0.sp
                                    ),
                                    color = if (isMyMessage) chatTheme.myTextColor else chatTheme.peerTextColor
                                )
                            }

                            MessageType.FILE -> {
                                FileMessageDisplay(
                                    message,
                                    isMyMessage = isMyMessage,
                                    chatTheme
                                )
                            }

                            MessageType.ANNOUNCEMENTS -> TODO()
                        }

                        Spacer(Modifier.size(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = timeToString(message.seconds),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = if(isMobiles) 10.sp else 12.sp,
                                color = ChatColors.TIME_TEXT
                            )
                        }
                    }
                }
//                Box(Modifier.height(10.dp))
            }
            if (isMyMessage) {
                if (sameUser) {
                    Spacer(Modifier.width(6.dp))
                } else {
                    Column {
                        Triangle(false, chatTheme.myMessageColor)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileMessageDisplay(
    message: Message,
    isMyMessage: Boolean,
    chatTheme: ChatTheme
) {
    val fileRepository: FileRepository = koinInject()
    val chatViewModel: ChatViewModel = koinInject()
    val fileInfo = message.fileInfo!!
    val isMobiles = isPlatformMobile()

    val downloadedFileIds by chatViewModel.downloadedFileIds.collectAsStateWithLifecycle()
    val downloadProgressMap by chatViewModel.downloadProgress.collectAsStateWithLifecycle()

    val isDownloaded = fileRepository.doesSharedFileExist(fileInfo.fileId, fileInfo.dir) || downloadedFileIds.contains(fileInfo.fileId)
    val currentProgress = downloadProgressMap[fileInfo.fileId]
    val internalPath = fileRepository.getFileInternalPath(fileInfo.fileId, fileInfo.dir)

    val isVisualMedia =
        fileInfo.mimeType.startsWith("image/") || fileInfo.mimeType.startsWith("video/")

    val textColor =
        if (isMyMessage) ChatTheme.default().myTextColor else ChatTheme.default().peerTextColor

    val fileSizeFormatted = when {
        fileInfo.sizeInBytes > 1024 * 1024 -> "%.1f MB".format(fileInfo.sizeInBytes / (1024.0 * 1024.0))
        fileInfo.sizeInBytes > 1024 -> "%d KB".format(fileInfo.sizeInBytes / 1024)
        else -> "${fileInfo.sizeInBytes} B"
    }

    val onDownloadClick: () -> Unit = {
        chatViewModel.onDownloadFile(message)
    }

    Column {
        if (isDownloaded && isVisualMedia) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { fileRepository.openFileInDefaultApp(internalPath) }
            ) {
                // Use when to decide which preview to show
                when {
                    fileInfo.mimeType.startsWith("image/") -> {
                        SubcomposeAsyncImage(
                            model = File(internalPath),
                            contentDescription = "Image preview",
                            modifier = Modifier.widthIn(max = 300.dp)
                        )
                    }

                    fileInfo.mimeType.startsWith("video/") -> {
                        VideoPlayer(
                            filePath = internalPath,
                            modifier = Modifier.widthIn(max = 300.dp).aspectRatio(16 / 9f)
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 250.dp)
                .clickable {
                    if (isDownloaded) {
                        fileRepository.openFileInDefaultApp(
                            fileRepository.getFileInternalPath(
                                fileInfo.fileId,
                                fileInfo.dir
                            )
                        )
                    } else if (currentProgress == null) {
                        chatViewModel.onDownloadFile(message)
                    }
                }
        ) {
            if (!isVisualMedia || !isDownloaded) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "File",
                    tint = textColor,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = fileInfo.originalFileName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = if(isMobiles) 12.sp else 18.sp,
                    color = textColor
                )
                Text(
                    text = fileSizeFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = if(isMobiles) 12.sp else 18.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }

            if (!isDownloaded && currentProgress == null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDownloadClick() }
                )
            }
        }

        if (currentProgress != null) {
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
            )
        }

        if (message.text != null) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if(isMobiles) 14.sp else 18.sp,
                    letterSpacing = 0.sp
                ),
                color = if (isMyMessage) chatTheme.myTextColor else chatTheme.peerTextColor
            )
        }
    }
}


class TriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = if (risingToTheRight) {
            Path().apply {
                moveTo(x = 0f, y = size.height)
                lineTo(x = size.width, y = 0f)
                lineTo(x = size.width, y = size.height)
            }
        } else {
            Path().apply {
                moveTo(x = 0f, y = 0f)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)
            }
        }

        return Outline.Generic(path = trianglePath)
    }
}