package org.asv.looplink.ui

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFormattedDate(timestampSeconds: Long): String