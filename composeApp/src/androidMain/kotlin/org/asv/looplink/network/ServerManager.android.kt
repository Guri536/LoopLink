package org.asv.looplink.network

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService

actual class ServerManager(private val context: Context) {
    actual fun start(
        userUid: String,
        userName: String
    ) {
        val intent = Intent(context, P2PService::class.java).also {
            it.action = P2PService.ACTION_START
            it.putExtra("USER_UID", userUid)
            it.putExtra("USER_NAME", userName)
            context.startForegroundService(it)
        }
    }

    actual fun stop() {
        Intent(context, P2PService::class.java).also {
            it.action = P2PService.ACTION_STOP
            context.startService(it)
        }
    }

    actual fun close() {
        Intent(context, P2PService::class.java).also {
            it.action = P2PService.ACTION_CLOSE
            context.startService(it)
        }
    }
}