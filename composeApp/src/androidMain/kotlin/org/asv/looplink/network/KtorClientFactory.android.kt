package org.asv.looplink.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

internal actual fun httpClientEngine(): HttpClientEngine {
    return CIO.create {
//        connectTimeout = 100_000
//        socketTimeout = 100_000
        requestTimeout = 30_000
    }
}