package org.asv.looplink.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
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

@Composable
fun SendMessage(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showEmojiPanel by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun send() {
        if (inputText.isNotBlank() && inputText.isNotEmpty()) {
            sendMessage(inputText)
            inputText = ""
        }
    }

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
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji Picker",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable{
                            showEmojiPanel = !showEmojiPanel
                        }
                        .pointerHoverIcon(PointerIcon.Hand)
                )
            },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                send()
                            }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Send,
                            contentDescription = "Send"
                        )
                        Text("  Send")
                    }
                }
            }
        )
    }
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