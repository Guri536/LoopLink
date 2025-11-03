package org.asv.looplink.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
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

    private var currentUserUid: String? = null
    private var currentUserName: String? = null

    private val connectionManager: ConnectionManager by inject()
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CLOSE = "ACTION_CLOSE"
        private const val NOTIFICATION_CHANNEL_ID = "P2P_SERVICE_CHANNEL"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            println("P2PService: START_STICKY restart detected.")
            if (currentUserUid != null && currentUserName != null) {
                startAsForegroundService()
                startServer(currentUserUid!!, currentUserName!!)
            } else {
                println("P2PService: No user info, cannot restart. Stopping.")
                stopSelf()
            }
            return START_STICKY
        }

        when(intent?.action){
            ACTION_START -> {
                println("P2PService: Starting Ktor Server")
                val uid = intent.getStringExtra("USER_UID")
                val name = intent.getStringExtra("USER_NAME")

                if (uid == null || name == null) {
                    println("P2PService: Error - UID or Name is null on START.")
                    stopSelf()
                    return START_NOT_STICKY
                }

                currentUserUid = uid
                currentUserName = name

                startAsForegroundService()
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

    private fun startAsForegroundService() {
        val channelId = NOTIFICATION_CHANNEL_ID
        val channel = NotificationChannel(
            channelId,
            "LoopLink P2P Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("LoopLink Running")
            .setContentText("Your local P2P server is active")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
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