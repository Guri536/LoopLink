package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.db.LLData
import java.util.Properties

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual class DriverFactory actual constructor(){
    actual fun createDriver(): SqlDriver {
        val driver =  JdbcSqliteDriver("jdbc:sqlite:LLData.sq", Properties(),
            schema = LLData.Schema
        )
        return driver
    }
}

actual fun getPlatformType(): PlatformType{
    return PlatformType.DESKTOP
}