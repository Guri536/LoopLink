package org.asv.looplink.network

import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel

expect class ServerManager{
    fun start(
        port: Int,
        userUid: String,
        userName: String,
        chatViewModel: ChatViewModel,
        peerDiscoveryViewModel: PeerDiscoveryViewModel?
    )
    fun stop()
    fun close()
}