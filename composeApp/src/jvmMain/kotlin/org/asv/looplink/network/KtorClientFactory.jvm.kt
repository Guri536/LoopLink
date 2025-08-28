package org.asv.looplink.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO


internal actual fun httpClientEngine(): HttpClientEngine {
    return CIO.create{
        requestTimeout = 30_000
    }
}