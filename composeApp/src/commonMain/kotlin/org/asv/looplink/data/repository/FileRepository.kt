package org.asv.looplink.data.repository

import org.asv.looplink.data.model.ManagedFile

data object DIRECTORIES {
    const val AppDIR = "appData"
    const val ConnDIR = "connections"
    const val FilesDIR = "shared_files"
    const val UserDIR = "user"
}

expect class FileRepository {
    /**
     * Copies a file from an external source (given by its path/URI)
     * into the app's internal storage.
     * @return A ManagedFile object on success, or null on failure.
     */
    suspend fun copyFileToInternalStorage(sourcePath: String): ManagedFile?

    /**
     * Deletes a file from the app's internal storage.
     * @return True if deletion was successful.
     */
    suspend fun deleteInternalFile(internalPath: String): Boolean

    fun sanitizeFileName(name: String): String

    suspend fun copyBlobToFile(blob: ByteArray, uid: String, dir: String = "data"): String

    suspend fun getFileBytes(path: String): ByteArray?
}