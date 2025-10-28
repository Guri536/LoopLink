package org.asv.looplink.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import org.asv.looplink.data.model.ManagedFile
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

actual class FileRepository(private val context: Context) {
    private val appDir = File(context.filesDir, "shared_files").apply { mkdirs() }
    private val connDir = File(context.filesDir, "connections").apply { mkdirs() }
    private val userDir = File(context.filesDir, "user").apply { mkdirs() }
    actual fun sanitizeFileName(name: String): String {
        // Remove path separators and dangerous characters
        val sanitized = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        // Prevent path traversal
        val clean = sanitized.substringAfterLast('/').substringAfterLast('\\')

        // Truncate absurdly long names (some providers send 200+ chars)
        return clean.take(255)
    }




    actual suspend fun copyFileToInternalStorage(sourcePath: String): ManagedFile? {
        return try {
            println("FileRepository: Starting copy from: $sourcePath")

            val sourceUri = if (sourcePath.startsWith("content://") || sourcePath.startsWith("file://")) {
                sourcePath.toUri()
            } else {
                // It's a plain file path, convert it to file:// URI
                File(sourcePath).toUri()
            }

            val scheme = sourceUri.scheme

            println("FileRepository: URI scheme: $scheme")

            // Handle different URI schemes
            when (scheme) {
                "content" -> copyFromContentUri(sourceUri)
                "file" -> copyFromFilePath(sourceUri)
                else -> {
                    println("FileRepository: Unsupported URI scheme: $scheme")
                    null
                }
            }
        } catch (e: Exception) {
            println("FileRepository: Error - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun copyFromContentUri(sourceUri: Uri): ManagedFile? {
        return try {
            var fileName: String? = null
            var fileSize = 0L

            println("FileRepository: Querying content URI")

            // Query the content provider for file metadata
            context.contentResolver.query(sourceUri, null, null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }

            println("FileRepository: fileName=$fileName, fileSize=$fileSize")

            if (fileName == null) {
                // Fallback: generate a filename from the URI
                fileName = "file_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
                val mimeType = context.contentResolver.getType(sourceUri)
                val extension = getExtensionFromMimeType(mimeType)
                if (extension != null) {
                    fileName = "$fileName.$extension"
                }
            }

            val sanitizedFileName = sanitizeFileName(fileName!!)
            val destinationFile = File(appDir, sanitizedFileName)

            // Prevent path traversal
            if (!destinationFile.canonicalPath.startsWith(appDir.canonicalPath)) {
                throw SecurityException("Invalid file path: ${destinationFile.path}")
            }

            println("FileRepository: Copying to: ${destinationFile.absolutePath}")

            // Copy the file
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Get actual file size if not available from cursor
            if (fileSize == 0L) {
                fileSize = destinationFile.length()
            }

            val mimeType = context.contentResolver.getType(sourceUri) ?: "application/octet-stream"

            println("FileRepository: Success! Internal path: ${Uri.fromFile(destinationFile)}")

            ManagedFile(
                internalPath = Uri.fromFile(destinationFile).toString(),
                originalFileName = fileName,
                mimeType = mimeType,
                sizeInBytes = fileSize
            )
        } catch (e: Exception) {
            println("FileRepository: copyFromContentUri error - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun copyFromFilePath(sourceUri: Uri): ManagedFile? {
        return try {
            val sourceFile = File(sourceUri.path!!)

            if (!sourceFile.exists()) {
                println("FileRepository: Source file doesn't exist: ${sourceFile.absolutePath}")
                return null
            }

            val fileName = sourceFile.name
            val fileSize = sourceFile.length()
            val sanitizedFileName = sanitizeFileName(fileName)
            val destinationFile = File(appDir, sanitizedFileName)

            // Prevent path traversal
            if (!destinationFile.canonicalPath.startsWith(appDir.canonicalPath)) {
                throw SecurityException("Invalid file path: ${destinationFile.path}")
            }

            println("FileRepository: Copying file from ${sourceFile.absolutePath} to ${destinationFile.absolutePath}")

            // Copy the file
            sourceFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Guess MIME type from extension
            val extension = fileName.substringAfterLast('.', "")
            val mimeType = getMimeTypeFromExtension(extension) ?: "application/octet-stream"

            println("FileRepository: Success! Internal path: ${Uri.fromFile(destinationFile)}")

            ManagedFile(
                internalPath = Uri.fromFile(destinationFile).toString(),
                originalFileName = fileName,
                mimeType = mimeType,
                sizeInBytes = fileSize
            )
        } catch (e: Exception) {
            println("FileRepository: copyFromFilePath error - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun getExtensionFromMimeType(mimeType: String?): String? {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/mpeg" -> "mpg"
            "application/pdf" -> "pdf"
            "text/plain" -> "txt"
            else -> null
        }
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mpg", "mpeg" -> "video/mpeg"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> null
        }
    }

    actual suspend fun deleteInternalFile(internalPath: String): Boolean {
        return try {
            File(internalPath.toUri().path!!).delete()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun copyBlobToFile(blob: ByteArray, uid: String): String {
        val file = File(userDir, "pfp_$uid.jpg")
        FileOutputStream(file).use { output ->
            output.write(blob)
        }

        return file.absolutePath
    }
}