package org.asv.looplink.data.database
//
//import app.cash.sqldelight.db.SqlDriver
//import com.db.LLData
//import org.asv.looplink.data.repository.FileRepository
//import kotlinx.coroutines.runBlocking
//import org.asv.looplink.DatabaseMng
//import org.koin.compose.koinInject
//import org.koin.java.KoinJavaComponent.get
//
//class MigrationManager(
//    private val driver: SqlDriver,
//    private val fileRepository: FileRepository
//) {
//    fun migrateProfilePictures() {
//        val database = LLData(driver)
//
//        // Get all users with BLOB data but no path
//        val usersToMigrate = database.lLDataQueries.selectAll().executeAsList()
//            .filter { it.pfpImage != null && (it.pfpPath == null || it.pfpPath.isEmpty()) }
//
//        if (usersToMigrate.isEmpty()) {
//            println("MigrationManager: No profile pictures to migrate")
//            return
//        }
//
//        println("MigrationManager: Found ${usersToMigrate.size} profile pictures to migrate")
//
//        usersToMigrate.forEach { user ->
//            try {
//                // Save BLOB to temporary file first
//                val tempFilePath = saveBlobToTempFile(user.pfpImage!!, user.uid)
//
//                // Use FileRepository to copy to internal storage
//                runBlocking {
//                    val managedFile = fileRepository.copyFileToInternalStorage(tempFilePath)
//
//                    if (managedFile != null) {
//                        // Update database with file path
//                        database.lLDataQueries.updatePfpPath(managedFile.internalPath, user.uid)
//
//                        // Clear the BLOB data to save space
//                        database.lLDataQueries.clearPfpImage(user.uid)
//
//                        println("MigrationManager: Successfully migrated profile picture for user: ${user.name}")
//
//                        // Delete temp file
//                        deleteTempFile(tempFilePath)
//                    } else {
//                        println("MigrationManager: Failed to migrate profile picture for user: ${user.name}")
//                    }
//                }
//            } catch (e: Exception) {
//                println("MigrationManager: Error migrating profile picture for user: ${user.name}, error: ${e.message}")
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun saveBlobToTempFile(blob: ByteArray, uid: String): String {
//        // This will be implemented per platform
//        return saveBlobToTempFileImpl(blob, uid)
//    }
//
//    private fun deleteTempFile(path: String) {
//        try {
//            deleteTempFileImpl(path)
//        } catch (e: Exception) {
//            println("MigrationManager: Failed to delete temp file: $path")
//        }
//    }
//}
//
//// Platform-specific expect functions
//expect fun saveBlobToTempFileImpl(blob: ByteArray, uid: String): String
//expect fun deleteTempFileImpl(path: String)