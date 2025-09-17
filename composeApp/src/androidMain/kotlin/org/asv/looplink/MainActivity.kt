package org.asv.looplink

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.asv.looplink.network.AndroidKtorServer
import org.asv.looplink.webDriver.cuimsAPI

class MainActivity : ComponentActivity() {

    private lateinit var serverManager: AndroidKtorServer

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        serverManager = AndroidKtorServer(applicationContext)
        serverManager.onServerPortChanged = {
            port ->
            if(port > 0){
                println("Mainactivity: Server started on port $port")
            } else {
                println("Mainactivity: Server failed to start or port not available")
            }
        }

        println("Mainactivity: Starting server...")
        serverManager.start(port = 8080)

        val database = DatabaseMng(DriverFactory(this).createDriver())
        val cuimsAPI = cuimsAPI(WebView(this))
        setContent {
            App(database, cuimsAPI)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        println("Mainactivity: Stopping server...")
        serverManager.close()
    }

}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}