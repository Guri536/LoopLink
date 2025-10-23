package org.asv.looplink.components.fabButtons

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Represents a sub-item for a Floating Action Button (FAB) with customized iconSVG and background tints.
 * Sub-items are secondary action buttons that appear when the main FAB is expanded.
 */
interface FabButtonSub {
    val iconTint: Color
    val backgroundTint: Color
}

/**
 * Implementation of [FabButtonSub] interface.
 *
 * @property iconTint The [Color] used to tint the iconSVG of the sub-item.
 * @property backgroundTint The [Color] used to tint the background of the sub-item.
 */
private class FabButtonSubImpl(
    override val iconTint: Color,
    override val backgroundTint: Color
) : FabButtonSub

/**
 * Creates a new instance of [FabButtonSub] with the provided iconSVG and background tints.
 *
 * @param backgroundTint The [Color] used to tint the background of the sub-item.
 * @param iconTint The [Color] used to tint the iconSVG of the sub-item.
 * @return A new instance of [FabButtonSub] with the specified iconSVG and background tints.
 */
@Composable
fun FabButtonSub(
    backgroundTint: Color = MaterialTheme.colorScheme.secondary,
    iconTint: Color = MaterialTheme.colorScheme.onSecondary
): FabButtonSub = FabButtonSubImpl(iconTint, backgroundTint)