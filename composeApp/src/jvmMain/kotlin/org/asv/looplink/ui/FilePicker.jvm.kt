package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FilePicker(show: Boolean, mode: FilePickerMode, onFileSelected: (String?) -> Unit){
    if(show){
        val fileChooser = JFileChooser()
        if(mode == FilePickerMode.MEDIA_ONLY){
            fileChooser.addChoosableFileFilter(
                FileNameExtensionFilter(
                    "Media Files",
                    "jpg", "jpeg", "png", "gif",
                    "mp3", "wav", "ogg",
                    "mp4", "avi", "mov"
                )
            )
            fileChooser.isAcceptAllFileFilterUsed = false
        } else {
            fileChooser.isAcceptAllFileFilterUsed = true
        }
        val result = fileChooser.showOpenDialog(null)
        if(result == JFileChooser.APPROVE_OPTION){
            onFileSelected(fileChooser.selectedFile.absolutePath)
        } else {
            onFileSelected(null)
        }
    }
}