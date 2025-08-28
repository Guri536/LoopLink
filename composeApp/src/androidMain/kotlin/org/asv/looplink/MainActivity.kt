package org.asv.looplink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import org.asv.looplink.network.discovery.LANServiceDiscovery
import org.asv.looplink.network.AndroidKtorServer

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
        setContent {
            App(database)
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