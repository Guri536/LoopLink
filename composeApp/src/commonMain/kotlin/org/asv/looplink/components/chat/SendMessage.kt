package org.asv.looplink.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chaintech.videoplayer.util.isMobile
import org.asv.looplink.isPlatformMobile
import org.asv.looplink.ui.FilePicker
import org.asv.looplink.ui.FilePickerMode
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.compose.koinInject
import java.io.File

@Composable
fun SendMessage(
    modifier: Modifier = Modifier,
    roomId: Int,
) {
    var inputText by remember { mutableStateOf("") }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var attachedFilePath by remember { mutableStateOf<String?>(null) }
    val chatViewModel: ChatViewModel = koinInject()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isMobile = isPlatformMobile()

    fun send() {
        if (inputText.isNotBlank() || attachedFilePath != null) {
            chatViewModel.sendMessage(roomId, inputText, attachedFilePath)
            inputText = ""
            attachedFilePath = null
        }
    }

    FilePicker(
        show = showFilePicker,
        mode = FilePickerMode.ALL_FILES,
        onFileSelected = { filePath ->
            showFilePicker = false
            if (filePath != null) {
                attachedFilePath = filePath
            }
        }
    )

    Column(
        modifier = modifier
    ) {
        AnimatedVisibility(
            showEmojiPanel,
        ) {
            EmojiPanel(
                onEmojiSelected = {
                    inputText += it
                },
                modifier = Modifier
                    .width(300.dp)
                    .focusable(true)
            )
        }

        AnimatedVisibility(visible = attachedFilePath != null) {
            AttachedFileChip(
                filePath = attachedFilePath,
                onClear = { attachedFilePath = null }
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .onKeyEvent {
                    if (it.isCtrlPressed
                        && it.key == Key.Enter
                    ) {
                        send()
                        true
                    } else {
                        false
                    }
                }
                .border(BorderStroke(0.dp, color = Color.Transparent))
                .padding(10.dp)
                .focusRequester(focusRequester)
                .align(alignment = Alignment.CenterHorizontally),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.White,
                unfocusedPlaceholderColor = Color.White,
                focusedPlaceholderColor = Color.Black,
                cursorColor = Color.Black,
                unfocusedTrailingIconColor = Color.White,
                focusedTrailingIconColor = Color.Black,
                unfocusedLabelColor = Color.White,
                focusedLabelColor = Color.Black,
            ),
            value = inputText,
            placeholder = {
                Text("Type message...")
            },
            onValueChange = {
                inputText = it
            },
            maxLines = 4,
            leadingIcon = {
                IconButton(
                    onClick = { showEmojiPanel = !showEmojiPanel },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji Picker",
                    )
                }
            },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { showFilePicker = true }, modifier = Modifier
                            .padding(end = 4.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach File"
                        )
                    }

                    Spacer(Modifier.width(
                        if (isMobile) 0.dp else 20.dp
                    ))

                    IconButton(
                        onClick = { send() },
                        enabled = inputText.isNotEmpty() || attachedFilePath != null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun AttachedFileChip(filePath: String?, onClear: () -> Unit) {
    if (filePath == null) return

    val fileName = remember(filePath) { File(filePath).name }

    AssistChip(
        modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
        onClick = {},
        label = { Text(fileName) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onClear, modifier = Modifier.size(AssistChipDefaults.IconSize)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attached file"
                )
            }
        }
    )
}

@Composable
fun EmojiPanel(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf(
        "😀", "😂", "😍", "😎", "👍", "🎉", "❤️", "🔥", "👏", "😢",
        "😅", "🙌", "🤔", "😴", "🥳", "🤯", "😡", "🤡", "💀", "👀"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F1F1))
            .padding(6.dp)
    ) {
        items(emojis) { emoji ->
            Text(
                emoji,
                fontSize = 26.sp,
                modifier = Modifier
//                    .padding(6.dp)
                    .clickable { onEmojiSelected(emoji) }
            )
        }
    }
}