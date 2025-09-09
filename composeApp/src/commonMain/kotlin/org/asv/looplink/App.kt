package org.asv.looplink

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import org.asv.looplink.errors.errorsLL
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.theme.Colors
import org.asv.looplink.webDriver.cuimsAPI
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Bitmap
import java.io.File

@Composable
fun App(database: DatabaseMng) {

    val coroutineScope = rememberCoroutineScope()
    val webSocketClient = createKtorClient()

    suspend fun sendMessage(
        peerIp: String,
        peerPort: Int,
        message: String
    ) {
        try {
            webSocketClient.webSocket(
                method = HttpMethod.Get,
                host = peerIp,
                port = peerPort,
                path = "/looplink/sync"
            ) {
                send(Frame.Text(message))
                for (frame in incoming) {
                    println("Recieved from server: ${frame.readBytes().decodeToString()}")
                }
            }
        } catch (e: Exception) {
            println("Error sending message to $peerIp, $peerPort: ${e.message}")
        }

    }

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val showSize = remember { mutableStateOf("Press") }
        var message by remember { mutableStateOf(("Send Message")) }
        val plat = getPlatform()


        fun updateShow() {
            if (showContent) {
                showContent = false
                showContent = true
            }
        }

        fun updateSize() {
            showSize.value = database.getSize().toString()
        }

        Column(
            modifier = Modifier
                .background(Colors.DarkColorScheme.surface)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginFields()
        }
    }
}

@Composable
fun LoginFields() {
    var uidField by remember { mutableStateOf("") }
    var passField by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    var isUIDError by remember { mutableStateOf(false) }
    var isPassError by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var captchaFile by remember { mutableStateOf(ImageBitmap(width = 0, height = 0)) }
    var captchaField by remember { mutableStateOf("") }
    var showCaptcha by remember { mutableStateOf(false) }
    var isCaptchaError by remember { mutableStateOf(false) }
    var webDriver by remember { mutableStateOf(cuimsAPI()) }

    val fontSize = 20.sp

    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = if (isHovered) Color.White else Color(0xFFFAFAFA),
        disabledContainerColor = Color.Gray,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
    )
    val textStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = fontSize
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier,
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .width(IntrinsicSize.Max)
            ) {

                Row(
                    modifier = Modifier
                        .padding(Dp(5f))
                        .fillMaxWidth(),
                    Arrangement.spacedBy(10.dp, Alignment.End),
                    Alignment.CenterVertically

                ) {
                    Text(
                        text = "UID     ",
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    )


                    TextField(
                        modifier = Modifier,
                        value = uidField,
                        placeholder = {
                            Text(
                                "Enter UID",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onValueChange = {
                            uidField = it
                        },
                        colors = colors,
                        singleLine = true,
                        textStyle = textStyle,
                        isError = isUIDError || isError,
                        supportingText = {
                            if (isUIDError) {
                                TextFieldFooterErrorMsg("UID cannot be empty")
                            } else if (isError) {
                                TextFieldFooterErrorMsg(errorMessage)
                            }
                        },
                        shape = MaterialTheme.shapes.large
                    )

                }
                Spacer(modifier = Modifier.width(20.dp))
                Row(
                    modifier = Modifier
                        .padding(Dp(5f))
                        .fillMaxWidth(),
                    Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    Alignment.CenterVertically,

                    ) {

                    Text(
                        text = "Password",
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = passField,
                        placeholder = {
                            Text(
                                "Enter Password",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onValueChange = { it ->
                            passField = it
                        },
                        singleLine = true,
                        colors = colors,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = textStyle,
                        isError = isPassError || isError,
                        supportingText = {
                            if (isPassError) {
                                TextFieldFooterErrorMsg("Password cannot be empty")
                            } else if (isError) {
                                TextFieldFooterErrorMsg(errorMessage)
                            }
                        },
                        shape = MaterialTheme.shapes.large
                    )
                }

            }

            if (showCaptcha) {
                Row(
                    modifier = Modifier.background(Color.Red)
//                        .wrapContentHeight()
                    ,
                    Arrangement.Center,
                    Alignment.Top
                ) {
                    TextField(
                        value = captchaField,
                        placeholder = {
                            Text(
                                "Enter Captcha",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onValueChange = { it ->
                            captchaField = it
                        },
                        singleLine = true,
                        colors = colors,
                        textStyle = textStyle,
                        isError = isCaptchaError || isError,
                        supportingText = {
                            if (isCaptchaError) {
                                TextFieldFooterErrorMsg("Captcha cannot be empty")
                            } else if (isError) {
                                TextFieldFooterErrorMsg(errorMessage)
                            }
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        ),
                        modifier = Modifier
                            .background(Color.Yellow)
                            .height(IntrinsicSize.Min)
                    )
                    Box(
                        modifier = Modifier
//                            .aspectRatio(3.33f)
//                            .border(
//                                border = BorderStroke(2.dp, Color.Red),
//                                shape = RoundedCornerShape(
//                                    topStart = 0.dp,
//                                    topEnd = 16.dp,
//                                    bottomEnd = 16.dp,
//                                    bottomStart = 0.dp
//                                )
//                            )
                            .background(Color.Blue),
                        contentAlignment = Alignment.Center

                    ) {
                        Image(
                            bitmap = captchaFile,
                            contentDescription = "Captcha Image",
                        )
                    }
                }

            }

            Box(
            ) {
                Button(
                    onClick = {
                        if (!showCaptcha) {
                            isUIDError = false
                            isPassError = false
                            isError = false
                            if (uidField.isBlank()) {
                                isUIDError = true
                                errorMessage = "UID cannot be empty"
                                return@Button
                            }
                            if (passField.isBlank()) {
                                isPassError = true
                                errorMessage = "Password cannot be empty"
                                return@Button
                            }

                            try {
                                webDriver = cuimsAPI(uidField, passField)
                            } catch (e: Exception) {
                                isError = true
                                errorMessage = errorsLL.internet_error
                            }
                            val loginSuccess = webDriver.login()

                            if (!loginSuccess.success) {
                                isError = true
                                errorMessage = loginSuccess.message
                                return@Button
                            }

                            val imgFile = webDriver.getCaptcha()
                            if (!imgFile.first.success) {
                                isError = true
                                errorMessage = imgFile.first.message
                                return@Button
                            }

                            captchaFile = imgFile.second!!
                            showCaptcha = true
                        } else {
                            if (captchaField.isBlank()) {
                                isCaptchaError = true
                                errorMessage = "Captcha cannot be empty"
                                return@Button
                            }
                        }
                    }
                ) {
                    Text(
                        "Submit",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webDriver.endSession()
        }
    }

}

@Composable
fun TextFieldFooterErrorMsg(text: String = "Error") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Start,
        fontSize = 15.sp,
    )
}