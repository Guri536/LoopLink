package org.asv.looplink.data.repository

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.asv.looplink.data.model.ManagedFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JFileChooser
import kotlin.io.path.Path

actual class FileRepository {
    private val appDir = File(System.getProperty("user.home"), ".looplink/${DIRECTORIES.AppDIR}").apply { mkdirs() }
    private val filesDir = File(System.getProperty("user.home"), ".looplink/${DIRECTORIES.FilesDIR}").apply { mkdirs() }
    private val connDir = File(System.getProperty("user.home"), ".looplink/${DIRECTORIES.ConnDIR}").apply { mkdirs() }
    private val userDir = File(System.getProperty("user.home"), ".looplink/${DIRECTORIES.UserDIR}").apply { mkdirs() }

    actual fun sanitizeFileName(name: String): String {
        // Remove path separators and dangerous characters
        val sanitized = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        // Prevent path traversal
        val clean = sanitized.substringAfterLast('/').substringAfterLast('\\')

        // Truncate absurdly long names (some providers send 200+ chars)
        return clean.take(255)
    }
    actual suspend fun copyFileToInternalStorage(sourcePath: String): ManagedFile? {
        return try{
            val sourceFile = File(sourcePath)
            if(!sourceFile.exists()) return null

            val safeFileName = sanitizeFileName(sourceFile.name)
            val destinationFile = File(appDir, safeFileName)

            // Prevent path traversal
            if (!destinationFile.canonicalPath.startsWith(appDir.canonicalPath)) {
                throw SecurityException("Invalid file path: ${destinationFile.path}")
            }

            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            ManagedFile(
                internalPath = destinationFile.absolutePath,
                originalFileName = sourceFile.name,
                mimeType = Files.probeContentType(destinationFile.toPath()) ?: "application/octet-stream",
                sizeInBytes = destinationFile.length()
            )
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    actual suspend fun deleteInternalFile(internalPath: String): Boolean {
        return try {
            File(internalPath).delete()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun copyBlobToFile(blob: ByteArray, uid: String, dir: String): String {
        val fileDir = File(System.getProperty("user.home"), ".looplink/$dir").apply { mkdirs() }
        val file = File(fileDir, "pfp_$uid.jpg")
        FileOutputStream(file).use {
            it.write(blob)
        }
        return file.absolutePath
    }

    actual suspend fun getFileBytes(path: String): ByteArray? {
        return withContext(Dispatchers.IO){
            try {
                val file = File(path)
                if(file.exists() && file.canRead()){
                    file.readBytes()
                } else {
                    println("FileRepository: File not found or unreadable at $path")
                    null
                }
            } catch (e: Exception){
                println("FileRepository: Error reading file bytes ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

}