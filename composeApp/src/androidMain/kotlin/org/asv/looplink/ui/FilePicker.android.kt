package org.asv.looplink.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.util.UUID

enum class FilePickerMode {
    MEDIA_ONLY,      // Images and Videos (uses Photo Picker on Android 13+)
    ALL_FILES        // All file types including documents
}

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    FilePickerWithMode(show, FilePickerMode.MEDIA_ONLY, onFileSelected)
}

@Composable
fun FilePickerWithMode(
    show: Boolean,
    mode: FilePickerMode = FilePickerMode.ALL_FILES,
    onFileSelected: (String?) -> Unit
) {
    val context = LocalContext.current

    // Photo Picker for media only (Android 13+, no permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            onFileSelected(null)
            return@rememberLauncherForActivityResult
        }

        // 2. CRITICAL: Immediately copy the file to survive process death
//        try {
//            // Get the original file name from the URI
//            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "temp_${UUID.randomUUID()}"
//            val destinationFile = File(context.cacheDir, fileName)
//
//            // Use the ContentResolver to get the data stream and copy it
//            context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                destinationFile.outputStream().use { outputStream ->
//                    inputStream.copyTo(outputStream)
//                }
//            }
//
//            // 3. Return the stable, absolute path to YOUR COPY of the file
//            onFileSelected(destinationFile.absolutePath)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            onFileSelected(null)
//        }
        handleSelectedUri(context, uri, onFileSelected)
    }

    // OpenDocument for all file types (no permissions needed on any Android version!)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        handleSelectedUri(context, uri, onFileSelected)
    }

    // Fallback GetContent (requires permissions on older Android)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        handleSelectedUri(context, uri, onFileSelected)
    }

    LaunchedEffect(show) {
        if (show) {
            try {
                when {
                    // For media only on Android 13+, use Photo Picker
                    mode == FilePickerMode.MEDIA_ONLY &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                println("Trying to get media only")
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                    // For all files, use OpenDocument (works on all Android versions)
                    mode == FilePickerMode.ALL_FILES -> {
                        println("Trying to get all files type")
                        documentPickerLauncher.launch(arrayOf("*/*"))
                    }
                    // Fallback for older Android with media only
                    else -> {
                        println("Fallback Option")
                        filePickerLauncher.launch("*/*")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFileSelected(null)
            }
        }
    }
}

private fun handleSelectedUri(
    context: android.content.Context,
    uri: Uri?,
    onFileSelected: (String?) -> Unit
) {
    if (uri == null) {
        onFileSelected(null)
        return
    }

    try {
        // Take persistable URI permission for documents
        if (uri.scheme == "content") {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Some URIs don't support persistable permissions, that's okay
                println("Cannot take persistable permission: ${e.message}")
            }
        }

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val fileName = cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                it.getString(nameIndex)
            } else {
                null
            }
        } ?: "temp_${UUID.randomUUID()}"
        println(fileName)

        val destinationFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        println(destinationFile.absolutePath)
        onFileSelected(destinationFile.absolutePath)

    } catch (e: Exception) {
        e.printStackTrace()
        onFileSelected(null)
    }
}