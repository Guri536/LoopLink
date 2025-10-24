package org.asv.looplink.network

expect class ServerManager{
    fun start(
        userUid: String,
        userName: String,
    )
    fun stop()
    fun close()
}