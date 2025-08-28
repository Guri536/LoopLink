package org.asv.looplink.network

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.cio.CIO

internal actual fun createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *> {
    return CIO
}