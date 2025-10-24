package org.asv.looplink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.asv.looplink.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val onLoginSuccess = {
                viewModel.startP2PServices()
            }
            App(
                onLoginSuccess
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
