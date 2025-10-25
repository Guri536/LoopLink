package org.asv.looplink.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.asv.looplink.theme.ChatTheme

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
fun ChatMessage(isMyMessage: Boolean, chatTheme: ChatTheme, message: Message, sameUser: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(top = (if(sameUser) 2.dp else 8.dp))
        ,
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
                Column {
                    UserPic(message.user)
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    Triangle(true, chatTheme.peerMessageColor)
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
                        .widthIn(max = 900.dp),
                ) {
                    Column {
                        if (!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = message.user.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = message.user.color,
                                    modifier = Modifier
                                )
                            }
                        }
                        Spacer(Modifier.size(3.dp))
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 18.sp,
                                letterSpacing = 0.sp
                            ),
                            color = if(isMyMessage) chatTheme.myTextColor else chatTheme.peerTextColor
                        )
                        Spacer(Modifier.size(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = timeToString(message.seconds),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = ChatColors.TIME_TEXT
                            )
                        }
                    }
                }
//                Box(Modifier.height(10.dp))
            }
            if (isMyMessage) {
                Column {
                    Triangle(false, chatTheme.myMessageColor)
                }
            }
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