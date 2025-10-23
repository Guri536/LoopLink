package org.asv.looplink.network

import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel

actual class ServerManager {
    actual fun start(
        port: Int,
        userUid: String,
        userName: String,
        chatViewModel: ChatViewModel,
        peerDiscoveryViewModel: PeerDiscoveryViewModel?
    ) {
        jvmKtorServerRunner.start(
            port,
            userUid,
            userName,
            chatViewModel,
            peerDiscoveryViewModel
        )
    }

    actual fun stop() {
        jvmKtorServerRunner.stop()
    }

    actual fun close() {
        jvmKtorServerRunner.close()
    }

}