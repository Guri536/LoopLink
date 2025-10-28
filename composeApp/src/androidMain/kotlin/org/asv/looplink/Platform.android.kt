package org.asv.looplink

import android.content.Context
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.db.LLData

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// This is the actual implementation of the DriverFactory expected from commonMain
actual class DriverFactory(val context: Context) {
    actual fun createDriver(): SqlDriver {
        println("Current schema version: ${LLData.Schema.version}")
        val driver = AndroidSqliteDriver(
            LLData.Schema,
            context,
            "LLData.db",
            callback = object : AndroidSqliteDriver.Callback(LLData.Schema) {
                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // 1. This handles the schema migration, adding the pfpPath column
                    super.onUpgrade(db, oldVersion, newVersion)
                }

            }
        )

        return driver
    }
}

actual fun getPlatformType(): PlatformType {
    return PlatformType.ANDROID
}

//actual class FileHelper(private val context: Context) {
//    actual fun saveBlobToFile(blob: ByteArray, uid: String): String {
//        val directory = File(context.filesDir, "profile_pictures")
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//        val file = File(directory, "pfp_$uid.jpg")
//        FileOutputStream(file).use { output ->
//            output.write(blob)
//        }
//
//        return file.absolutePath
//    }
//
//    actual fun getProfilePictureDirectory(): String {
//        return File(context.filesDir, "profile_pictures").absolutePath
//    }
//}