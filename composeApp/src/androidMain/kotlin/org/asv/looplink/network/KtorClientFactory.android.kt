package org.asv.looplink.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

internal actual fun httpClientEngine(): HttpClientEngine {
    return Android.create {
        connectTimeout = 100_000
        socketTimeout = 100_000
    }
}