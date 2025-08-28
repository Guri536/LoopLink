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
import org.asv.looplink.network.

class MainActivity : ComponentActivity() {

    private lateinit var serviceDiscovery: LANServiceDiscovery
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var registeredServerPort: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidKtorServer.start(serverScope)

        val database = DatabaseMng(DriverFactory(this).createDriver())
        setContent {
            App(database)
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}