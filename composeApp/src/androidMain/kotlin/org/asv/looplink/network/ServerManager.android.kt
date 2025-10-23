package org.asv.looplink.network

import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel

actual class ServerManager(private val androidKtorServer: AndroidKtorServer) {
    actual fun start(
        port: Int,
        userUid: String,
        userName: String,
        chatViewModel: ChatViewModel,
        peerDiscoveryViewModel: PeerDiscoveryViewModel?
    ) {
        androidKtorServer.start(
            port,
            userUid,
            userName,
            chatViewModel,
            peerDiscoveryViewModel
        )
    }

    actual fun stop() {
        androidKtorServer.stop()
    }

    actual fun close() {
        androidKtorServer.close()
    }
}