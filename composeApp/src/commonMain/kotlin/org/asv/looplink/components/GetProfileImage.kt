package org.asv.looplink.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GetProfileImage(bytes: ByteArray?, modifier: Modifier = Modifier)