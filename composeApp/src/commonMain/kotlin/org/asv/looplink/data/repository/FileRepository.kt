package org.asv.looplink.data.repository

import kotlinx.coroutines.flow.Flow
import org.asv.looplink.components.chat.ManagedFile
import java.io.File

enum class DIRECTORIES {
    AppDir, UserDir, FilesDir, ConnDir
}

fun getDirectoryName(dir: DIRECTORIES): String {
    return when(dir){
        DIRECTORIES.AppDir -> "looplink_data"
        DIRECTORIES.UserDir -> "user_files"
        DIRECTORIES.FilesDir -> "shared_files"
        DIRECTORIES.ConnDir -> "connection_files"
    }
}

expect class FileRepository {

    fun getDirectory(dir: DIRECTORIES): File
    suspend fun copyFileToInternalStorage(sourcePath: String, dir: DIRECTORIES = DIRECTORIES.FilesDir): ManagedFile?

    suspend fun deleteInternalFile(internalPath: String): Boolean
    suspend fun deleteSharedFile(fileId: String): Boolean

    suspend fun copyBlobToFile(blob: ByteArray, fileName: String, dir: DIRECTORIES = DIRECTORIES.FilesDir): ManagedFile?

    suspend fun getSharedFile(fileId: String): ByteArray?

    suspend fun getSharedFileAsFile(fileId: String): File?

    fun sanitizeFileName(name: String): String

    suspend fun getFileBytes(path: String): ByteArray?

    fun doesFileExist(filePath: String): Boolean

    fun doesSharedFileExist(fileId: String, dir: DIRECTORIES = DIRECTORIES.FilesDir): Boolean

    fun getFileInternalPath(fileId: String, dir: DIRECTORIES = DIRECTORIES.FilesDir): String
    fun openFileInDefaultApp(filePath: String)
}
