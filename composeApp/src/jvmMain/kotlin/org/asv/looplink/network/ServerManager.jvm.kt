package org.asv.looplink.network

import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.java.KoinJavaComponent.get
actual class ServerManager {
    actual fun start(
        userUid: String,
        userName: String,
    ) {
        val port = 0
        val chatViewModel: ChatViewModel = get(ChatViewModel::class.java)
        val peerDiscoveryViewModel: PeerDiscoveryViewModel = get(PeerDiscoveryViewModel::class.java)
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