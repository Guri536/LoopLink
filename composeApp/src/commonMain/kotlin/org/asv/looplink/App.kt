package org.asv.looplink


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.db.LLData
import com.db.LLDataQueries
import com.db.LoopLinkUser
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import org.asv.looplink.components.LocalCuimsApi
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LoginFields
import org.asv.looplink.components.SettingsPage
import org.asv.looplink.components.UserProfileCard
import org.asv.looplink.components.loadUserInfo
import org.asv.looplink.components.userInfo
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.operations.insertUserDataFromProfile
import org.asv.looplink.theme.Colors
import org.asv.looplink.webDriver.cuimsAPI
import org.jetbrains.skia.Image
import ui.theme.AppTheme
import java.util.Base64

@Composable
fun App(
    database: DatabaseMng,
    cuimsAPI: cuimsAPI
) {
    val coroutineScope = rememberCoroutineScope()
    val webSocketClient = createKtorClient()
//    val Navigator = Navigator()

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

    CompositionLocalProvider(
        LocalDatabase provides database,
        LocalCuimsApi provides cuimsAPI
    ) {
        AppTheme {
            var showContent by remember { mutableStateOf(false) }
            val showSize = remember { mutableStateOf("Press") }
            var message by remember { mutableStateOf(("Send Message")) }
            val plat = getPlatform()

            Column(
                modifier = Modifier
                    .background(Colors.DarkColorScheme.surface)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                println(database.getAllFromDatabase())
                if (database.getSize() == 0) {
                    Navigator(
                        LoginFields(
                            LocalCuimsApi.current,
                            loginSuccess = { it ->
                                insertUserDataFromProfile(database, it)
                            }
                        )
                    )
                } else {
                    Navigator(SettingsPage())
                }
            }
        }
    }
}