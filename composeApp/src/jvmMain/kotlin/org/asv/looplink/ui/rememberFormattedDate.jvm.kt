package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
actual fun rememberFormattedDate(timestampSeconds: Long): String {
    val t = remember(timestampSeconds) {
        val instant = Instant.fromEpochSeconds(timestampSeconds)
        val zone = TimeZone.currentSystemDefault()
        val messageDate = instant.toLocalDateTime(zone).date
        val today = Clock.System.now().toLocalDateTime(zone).date

        val messageJavaDate = messageDate.toJavaLocalDate()
        val todayJavaDate = today.toJavaLocalDate()

        val daysDiff = ChronoUnit.DAYS.between(messageJavaDate, todayJavaDate)

        when (daysDiff) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd MM, yyyy", Locale.getDefault())
                messageJavaDate.format(formatter)
            }
        }
    }
    return t
}