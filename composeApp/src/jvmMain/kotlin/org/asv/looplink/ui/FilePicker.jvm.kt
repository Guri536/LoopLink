package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit){
    if(show){
        val fileChooser = JFileChooser()
        val result = fileChooser.showOpenDialog(null)
        if(result == JFileChooser.APPROVE_OPTION){
            onFileSelected(fileChooser.selectedFile.absolutePath)
        } else {
            onFileSelected(null)
        }
    }
}