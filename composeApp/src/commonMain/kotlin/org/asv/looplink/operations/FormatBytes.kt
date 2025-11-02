package org.asv.looplink.operations

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "kB", "MB", "GB", "TB")

    // Calculate the log base 1024 to find the correct unit index
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    // Handle the base case (Bytes) separately to avoid 0.x B
    if (digitGroups == 0) return "$bytes B"

    val value = bytes / 1024.0.pow(digitGroups)

    // Rounds to one decimal place in a KMP-safe way
    val roundedValue = (value * 10).roundToInt() / 10.0
    return "$roundedValue ${units[digitGroups]}"
}