package org.asv.looplink.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint

internal actual fun httpClientEngine(): HttpClientEngine {
    return CIO.create{
        requestTimeout = 30_000
        maxConnectionsCount = 1000
//        endpoint{
//            connectTimeout = 5000
//            socketTimeout = 5000
//            keepAliveTime = 5000
//        }
    }
}