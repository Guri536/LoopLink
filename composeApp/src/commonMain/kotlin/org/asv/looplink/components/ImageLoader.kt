package org.asv.looplink.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
internal expect fun painterFromFile(path: String): Painter?