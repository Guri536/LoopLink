package org.asv.looplink.network

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.isActive
import org.asv.looplink.data.repository.ChatRepository
import java.util.concurrent.ConcurrentSkipListSet


class ConnectionManager(private val chatRepository: ChatRepository){

    val connectedPeerIps = ConcurrentSkipListSet<String>()

    fun addPeer(ip: String){
        connectedPeerIps.add(ip)
        println("ConnectionSecurity: Added peer $ip to authorized list.")
    }

    fun removePeer(ip: String) {
        connectedPeerIps.remove(ip)
        println("ConnectionSecurity: Removed peer $ip from authorized list.")
    }

    fun isPeerAuthorized(ip: String): Boolean {
        if (ip == "127.0.0.1" || ip == "localhost" || ip == "0:0:0:0:0:0:0:1") {
            return true
        }
        val isAuthorized = connectedPeerIps.contains(ip)
        if (!isAuthorized) {
            println("ConnectionSecurity: Denied request from unauthorized IP: $ip")
        }
        return isAuthorized
    }
}