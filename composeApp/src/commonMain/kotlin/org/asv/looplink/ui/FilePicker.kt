package org.asv.looplink.ui

import androidx.compose.runtime.Composable


enum class FilePickerMode {
    MEDIA_ONLY,
    ALL_FILES
}

@Composable
expect fun FilePicker(
    show: Boolean,
    mode: FilePickerMode = FilePickerMode.ALL_FILES,
    onFileSelected: (String?) -> Unit
)