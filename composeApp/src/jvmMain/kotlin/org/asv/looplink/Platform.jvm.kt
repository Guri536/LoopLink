package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.db.LLData
import java.util.Properties

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun isPlatformMobile(): Boolean = false

actual class DriverFactory{
    actual fun createDriver(): SqlDriver {
        val driver =  JdbcSqliteDriver("jdbc:sqlite:LLData.sq", Properties(),
            schema = LLData.Schema
        )
        println("DB Version: ${LLData.Schema.version}")
        return driver
    }
}

actual fun getPlatformType(): PlatformType{
    return PlatformType.DESKTOP
}

//private fun getVersion(driver: SqlDriver): Int {
//    return try {
//        driver.executeQuery(null, "PRAGMA user_version;", 0).use { cursor ->
//            cursor.next()
//            cursor.getLong(0)?.toInt() ?: 0
//        }
//    } catch (e: Exception) {
//        0
//    }
//}

//private fun setVersion(driver: SqlDriver, version: Int) {
//    driver.execute(null, "PRAGMA user_version = $version;", 0)
//}