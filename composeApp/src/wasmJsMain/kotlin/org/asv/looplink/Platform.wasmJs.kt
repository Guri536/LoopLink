package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
//import app.cash.sqldelight.d
import com.jetbrains.looplink.db.Database // Import your Database schema

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        TODO("To be configyred")
    }
}