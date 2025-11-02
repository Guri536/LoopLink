package org.asv.looplink.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual fun Modifier.backdropBlur(radius: Dp): Modifier {
    // Compose for Desktop does not support backdrop blur natively.
    // Return 'this' to do nothing.
    return this
}