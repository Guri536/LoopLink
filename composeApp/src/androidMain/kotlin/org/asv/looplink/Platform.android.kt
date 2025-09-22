package org.asv.looplink

import android.content.Context
import android.os.Build
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.db.LLData // Assuming this is your generated database class

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// This is the actual implementation of the DriverFactory expected from commonMain
actual class DriverFactory actual constructor() {
    private var context: Context? = null
    constructor( Appcontext: Context): this(){
        context = Appcontext
    }

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(LLData.Schema, context!!, "LLData.db") // Use a .db extension
    }
}

actual fun getPlatformType(): PlatformType {
    return PlatformType.ANDROID
}