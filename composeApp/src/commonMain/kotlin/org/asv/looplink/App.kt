package org.asv.looplink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import looplink.composeapp.generated.resources.Res
import looplink.composeapp.generated.resources.compose_multiplatform
import org.asv.looplink.network.createKtorClient

//import com.jetbrains.looplink.database

@Composable
@Preview
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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val plat = getPlatform()
            Text(
                "Hello there, you are on ${plat.name}\nLets see how this goes",
                modifier = Modifier,
                textAlign = TextAlign.Center
            )

            Button(onClick = {
                database.deleteUser()
                updateShow()
                updateSize()
            }) {
                Text("Delete Content")
            }
            Button(onClick = { showContent = !showContent }) {
                Text("Show Content")
            }
            Button(onClick = {
                database.insertIntoDatabase("A", database.getAllFromDatabase().size.toString())
                updateShow()
                updateSize()
            }) {
                Text("Add Content")
            }
            Button(onClick = { showSize.value = database.getSize().toString() }) {
                Text("Current Size: ${showSize.value}")
            }

            Button(onClick = {
                coroutineScope.launch {
                    sendMessage(
                        "192.168.29.137",
                        8080,
                        message
                    )
                }
            }) {
                Text("Send Message to JVM")
            }

            Button(onClick = {
                coroutineScope.launch {
                    sendMessage(
                        "192.168.29.191",
                        8080,
                        message
                    )
                }
            }) {
                Text("Send Message to Android")
            }
            TextField(
                value = message,
                onValueChange = { newText ->
                    message = newText
                }
            )

            if (showContent) {
                Text("Let's see")
                val data = database.getAllFromDatabase()
                Text(data.toString().prependIndent("What: "))
            }

        }
    }
}