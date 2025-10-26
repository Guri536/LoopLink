package org.asv.looplink.data.repository

import androidx.compose.runtime.Composable
import org.asv.looplink.data.model.ManagedFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JFileChooser
import kotlin.io.path.Path

actual class FileRepository {

    actual fun sanitizeFileName(name: String): String {
        // Remove path separators and dangerous characters
        val sanitized = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        // Prevent path traversal
        val clean = sanitized.substringAfterLast('/').substringAfterLast('\\')

        // Truncate absurdly long names (some providers send 200+ chars)
        return clean.take(255)
    }
    private val appDir = File(System.getProperty("user.home"), ".looplink/files").apply { mkdirs() }
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

}