package org.asv.looplink.data.model

data class ManagedFile(
    val internalPath: String,
    val originalFileName: String,
    val mimeType: String,
    val sizeInBytes: Long
)