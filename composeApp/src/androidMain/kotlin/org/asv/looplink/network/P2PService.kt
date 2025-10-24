package org.asv.looplink.network

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.asv.looplink.data.repository.ChatRepository
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.P2PState
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.android.ext.android.inject

class P2PService: Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val server: AndroidKtorServer by inject()
    private val chatViewModel: ChatViewModel by inject()
    private val chatRepository: ChatRepository by inject()

    private val connectionManager: ConnectionManager by inject()
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CLOSE = "ACTION_CLOSE"
        private const val NOTIFICATION_CHANNEL_ID = "P2P_SERVICE_CHANNEL"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_START -> {
                println("P2PService: Starting Ktor Server")
                val uid = intent.getStringExtra("USER_UID") ?: return START_NOT_STICKY
                val name = intent.getStringExtra("USER_NAME") ?: return START_NOT_STICKY
                startServer(uid, name)
            }
            ACTION_STOP -> {
                stopServer()
            }
            ACTION_CLOSE -> {
                closeServer()
            }
        }
        return START_STICKY
    }

    private fun startServer(uid: String, name: String){
        if(server.isRunning()) return
        serviceScope.launch {
            server.start(0, uid, name, chatViewModel, chatRepository, connectionManager)
        }
    }

    private fun stopServer(){
        serviceScope.launch {
            server.stop()
        }
    }

    private fun closeServer(){
        serviceScope.launch { server.close() }
    }

    override fun onBind(p0: Intent?): IBinder? = null
}