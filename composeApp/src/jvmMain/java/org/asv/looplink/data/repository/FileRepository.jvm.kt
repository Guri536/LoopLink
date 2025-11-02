package org.asv.looplink.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.asv.looplink.components.chat.ManagedFile
import java.awt.Desktop
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual class FileRepository {

    private val rootAppDir = File(System.getProperty("user.home"), ".looplink")
    private val appDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.AppDir)).apply { mkdirs() }
    private val filesDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.FilesDir)).apply { mkdirs() }
    private val connDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.ConnDir)).apply { mkdirs() }
    private val userDir =
        File(rootAppDir, getDirectoryName(DIRECTORIES.UserDir)).apply { mkdirs() }

    actual fun getDirectory(dir: DIRECTORIES): File {
        return when (dir) {
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
        return File(filePath).exists()
    }

    actual fun getFileInternalPath(fileId: String, dir: DIRECTORIES): String {
        val directory = getDirectory(dir)
        return File(directory, fileId).absolutePath
    }

    actual fun openFileInDefaultApp(filePath: String) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(File(filePath))
        } else {
            println("FileRepository: Desktop API is not supported")
        }
    }

    actual suspend fun copyFileToInternalStorage(
        sourcePath: String,
        dir: DIRECTORIES
    ): ManagedFile? {
        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists()) {
                    println("FileRepository: Unable to find file at $sourcePath")
                    return@withContext null
                }

                val originalFileName = sourceFile.name
                val fileSize = sourceFile.length()

                val fileExtension = originalFileName.substringAfterLast(".", "")
                val baseName = originalFileName.removeSuffix(".$fileExtension")
                val sanitizedBase = sanitizeFileName(baseName)
                val uniqueId =
                    "${sanitizedBase}_${System.currentTimeMillis()}" + (if (fileExtension.isNotEmpty()) ".$fileExtension" else "")

                val directory = getDirectory(dir)
                val destinationFile = File(directory, uniqueId)

                // Prevent path traversal
                if (!destinationFile.canonicalPath.startsWith(rootAppDir.canonicalPath)) {
                    throw SecurityException("Invalid file path: ${destinationFile.path}")
                }

                Files.copy(
                    sourceFile.toPath(),
                    destinationFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )

                return@withContext ManagedFile(
                    fileId = uniqueId,
                    originalFileName = sourceFile.name,
                    mimeType = Files.probeContentType(destinationFile.toPath())
                        ?: "application/octet-stream",
                    sizeInBytes = fileSize,
                    dir = dir
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    actual suspend fun deleteInternalFile(internalPath: String): Boolean {
        return try {
            File(internalPath).delete()
        } catch (e: Exception) {
            false
        }
    }

    actual fun doesSharedFileExist(fileId: String, dir: DIRECTORIES): Boolean {
        val directory = getDirectory(dir)
        return File(directory, fileId).exists()
    }

    actual suspend fun copyBlobToFile(
        blob: ByteArray,
        fileName: String,
        dir: DIRECTORIES
    ): ManagedFile? =
        withContext(Dispatchers.IO) {
            try {
                val directory = getDirectory(dir)
                val destinationFile = File(directory, fileName)
                destinationFile.writeBytes(blob)

                val mimeType =
                    Files.probeContentType(destinationFile.toPath()) ?: "application/octet-stream"

                ManagedFile(
                    fileId = fileName,
                    originalFileName = fileName, // We only know the ID
                    mimeType = mimeType,
                    sizeInBytes = blob.size.toLong(),
                    dir = dir
                )
            } catch (e: Exception) {
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
                    println("FileRepository: File not found or unreadable at $path")
                    null
                }
            } catch (e: Exception) {
                println("FileRepository: Error reading file bytes ${e.message}")
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
        return withContext(Dispatchers.IO) {
            try {

                val file = File(filesDir, fileId)

                if (file.exists() && file.canRead()) {
                    file
                } else {
                    println("FileRepository: Cannot access file or unreadabe for $fileId")
                    null
                }
            } catch (e: Exception) {
                println("FileRepository: Error getting $fileId file: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    actual suspend fun deleteSharedFile(fileId: String): Boolean {
        return File(filesDir, fileId).delete()
    }

}