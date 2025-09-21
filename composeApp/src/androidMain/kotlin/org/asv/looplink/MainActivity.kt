package org.asv.looplink

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.asv.looplink.network.AndroidKtorServer
import org.asv.looplink.webDriver.cuimsAPI

class MainViewModel(applicationContext: Context) : ViewModel() {
    val serverManager: AndroidKtorServer
    val database: DatabaseMng
    val cuimsAPI: cuimsAPI

    // 'init' is called only once when the ViewModel is first created
    init {
        println("MainViewModel: Initializing...")
        serverManager = AndroidKtorServer(applicationContext)
        database = DatabaseMng(DriverFactory(applicationContext).createDriver())
        cuimsAPI = cuimsAPI(WebView(applicationContext))

        // Start the server from a coroutine
        viewModelScope.launch {
            println("MainViewModel: Starting server...")
            serverManager.start(port = 8080)
        }
    }

    override fun onCleared() {
        println("MainViewModel: Stopping server...")
        serverManager.close()
        cuimsAPI.destroySession()
        super.onCleared()
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(applicationContext) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        println(viewModel.database.getAllFromDatabase())
        setContent()
        {
            App(viewModel.database, viewModel.cuimsAPI, peerDiscoveryViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
