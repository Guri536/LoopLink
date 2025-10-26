package org.asv.looplink.data.repository

import androidx.compose.runtime.Composable
import org.asv.looplink.data.model.ManagedFile

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
}