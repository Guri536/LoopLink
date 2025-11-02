package org.asv.looplink.data.repository

import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.asv.looplink.components.chat.ManagedFile
import java.io.File
import java.util.UUID

actual class FileRepository(private val context: Context) {
    private val rootAppDir = File(context.filesDir, "looplink").apply { mkdirs() }

    private val appDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.AppDir)).apply { mkdirs() }
    private val filesDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.FilesDir)).apply { mkdirs() }
    private val connDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.ConnDir)).apply { mkdirs() }
    private val userDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.UserDir)).apply { mkdirs() }
    actual fun getDirectory(dir: DIRECTORIES): File {
        return when(dir){
            DIRECTORIES.AppDir -> appDir
            DIRECTORIES.UserDir -> userDir
            DIRECTORIES.FilesDir -> filesDir
            DIRECTORIES.ConnDir -> connDir
        }
    }
    actual fun sanitizeFileName(name: String): String {
        // Remove path separators and dangerous characters
        val sanitized = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        // Prevent path traversal
        val clean = sanitized.substringAfterLast('/').substringAfterLast('\\')

        // Truncate absurdly long names (some providers send 200+ chars)
        return clean.take(255)
    }

    actual fun doesFileExist(filePath: String): Boolean {
        return File(filesDir, filePath).exists()
    }

    actual fun openFileInDefaultApp(filePath: String) {
        try {
            val file = File(filePath)
            val authority = "${context.packageName}.provider"

            val uri = FileProvider.getUriForFile(context, authority, file)
            val fileExtension = file.extension
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
                ?: "application/octet-stream"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            println("FileRepository: Error opening file $filePath - ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun getFileInternalPath(fileId: String, dir: DIRECTORIES): String {
        val directory = getDirectory(dir)
        return File(directory, fileId).absolutePath
    }

    actual suspend fun copyFileToInternalStorage(sourcePath: String, dir: DIRECTORIES): ManagedFile? {
        return try {
            println("FileRepository: Starting copy from: $sourcePath")

            val sourceUri =
                if (sourcePath.startsWith("content://") || sourcePath.startsWith("file://")) {
                    sourcePath.toUri()
                } else {
                    File(sourcePath).toUri()
                }

            var fileName: String? = null
            var fileSize = 0L

            // Get Metadata
            if (sourceUri.scheme == "content") {
                // Use ContentResolver for content:// URIs
                println("FileRepository: Using ContentResolver for content:// URI")
                context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        fileName =
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
                    }
                }
            } else if (sourceUri.scheme == "file") {
                // Use File API for file:/// URIs
                println("FileRepository: Using File API for file:// URI")
                sourceUri.path?.let { path ->
                    val file = File(path)
                    fileName = file.name
                    fileSize = file.length()
                }
            }

            if (fileName == null) {
                fileName = "file_${UUID.randomUUID()}"
            }

            val originalFileName = fileName
            val fileExtension = originalFileName.substringAfterLast(".", "")
            val baseName = originalFileName.removeSuffix(".$fileExtension")
            val sanitizedBase = sanitizeFileName(baseName)
            val uniqueId =
                "${sanitizedBase}_${System.currentTimeMillis()}" + (if (fileExtension.isNotEmpty()) ".$fileExtension" else "")

            val directory = getDirectory(dir)
            val destinationFile = File(directory, uniqueId)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) fileSize = destinationFile.length()

            val mimeTypeFromExtension = if (fileExtension.isNotEmpty()) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
            } else null

            val mimeType = mimeTypeFromExtension
                ?: context.contentResolver.getType(sourceUri)
                ?: "application/octet-stream"

            ManagedFile(
                fileId = uniqueId,
                originalFileName = originalFileName,
                mimeType = mimeType,
                sizeInBytes = fileSize
            )
        } catch (e: Exception) {
            println("FileRepository: Error - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    actual suspend fun deleteInternalFile(internalPath: String): Boolean {
        return try {
            File(internalPath.toUri().path!!).delete()
        } catch (e: Exception) {
            false
        }
    }

    actual fun doesSharedFileExist(fileId: String, dir: DIRECTORIES): Boolean {
        val directory = getDirectory(dir)
        return File(directory, fileId).exists()
    }

    actual suspend fun deleteSharedFile(fileId: String): Boolean{
        return try {
            File(filesDir, fileId).delete()
        } catch (e: Exception){
            false
        }
    }

    actual suspend fun copyBlobToFile(blob: ByteArray, fileName: String, dir: DIRECTORIES): ManagedFile? =
        withContext(Dispatchers.IO) {
            try {
                val directory = getDirectory(dir)
                val destinationFile = File(directory, fileName)
                destinationFile.writeBytes(blob)
                val mimeType = context.contentResolver.getType(destinationFile.toUri())
                    ?: "application/octet-stream"

                ManagedFile(
                    fileId = fileName,
                    originalFileName = fileName,
                    mimeType = mimeType,
                    sizeInBytes = blob.size.toLong(),
                    dir = dir
                )
            } catch (e: Exception) {
                println("FileRepository: saveReceivedFile error - ${e.message}")
                e.printStackTrace()
                null
            }
        }

    actual suspend fun getFileBytes(path: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    file.readBytes()
                } else {
                    println("File Repository: File not found or unreadable at $path")
                    null
                }
            } catch (e: Exception) {
                println("FileRepository: Error reading file bytes: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    actual suspend fun getSharedFile(fileId: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val safeFileName = sanitizeFileName(fileId)
                if (safeFileName != fileId) {
                    println("FileRepository: Access denied for malicious fileId: $fileId")
                    return@withContext null
                }

                val file = File(filesDir, fileId)
                if (file.exists() && file.canRead()) {
                    file.readBytes()
                } else {
                    null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    actual suspend fun getSharedFileAsFile(fileId: String): File? {
        return withContext(Dispatchers.IO){
            try {

            val file = File(filesDir, fileId)

            if(file.exists() && file.canRead()){
                file
            } else {
                println("FileRepository: Cannot access file or unreadabe for $fileId")
                null
            }
            } catch (e: Exception){
                println("FileRepository: Error getting $fileId file: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}