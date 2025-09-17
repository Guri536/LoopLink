package org.asv.looplink


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import org.asv.looplink.components.CustomOutlinedTextField
import org.asv.looplink.components.LoginFields
import org.asv.looplink.errors.errorsLL
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.theme.Colors
import org.asv.looplink.webDriver.cuimsAPI
import org.asv.looplink.webDriver.successLog
import ui.theme.AppTheme

@Composable
fun App(
    database: DatabaseMng,
    cuimsAPI: cuimsAPI
) {
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

    AppTheme {
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
            LoginFields(
                cuimsAPI,
                loginSuccess = {}
            )
        }
    }
}
