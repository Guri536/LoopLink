package org.asv.looplink.ui

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
actual fun rememberFormattedDate(timestampSeconds: Long): String {
    val context = LocalContext.current
    return remember(timestampSeconds) {
        val now = System.currentTimeMillis()
        val timeMillis = timestampSeconds * 1000

        when {
            DateUtils.isToday(timeMillis) -> "Today"
            DateUtils.isToday(timeMillis + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
            else -> {
                // Format for "Month Day, Year" e.g., "November 1, 2025"
                val sdf = SimpleDateFormat("dd MM, yyyy", Locale.getDefault())
                sdf.format(timeMillis)
            }
        }
    }
}