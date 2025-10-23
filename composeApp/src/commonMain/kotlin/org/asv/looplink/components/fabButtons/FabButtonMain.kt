package org.asv.looplink.components.fabButtons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the main floating action button (FAB) with an iconSVG and optional rotation.
 * The main FAB is the primary action button that can be expanded to reveal sub-items.
 */
interface FabButtonMain {
    val iconRes: ImageVector
    val iconRotate: Float?
}

/**
 * Implementation of [FabButtonMain] interface.
 *
 * @property iconRes The [ImageVector] representing the iconSVG to be displayed on the main FAB.
 * @property iconRotate The optional rotation angle for the main FAB iconSVG. If null, the iconSVG will not be rotated.
 */
private class FabButtonMainImpl(
    override val iconRes: ImageVector,
    override val iconRotate: Float?
) : FabButtonMain

/**
 * Creates a new instance of [FabButtonMain] with the provided iconSVG and optional rotation.
 *
 * @param iconRes The [ImageVector] representing the iconSVG to be displayed on the main FAB.
 * @param iconRotate The optional rotation angle for the main FAB iconSVG. If null, the iconSVG will not be rotated.
 * @return A new instance of [FabButtonMain] with the specified iconSVG and rotation.
 */
fun FabButtonMain(iconRes: ImageVector = Icons.Filled.Add, iconRotate: Float = 45f): FabButtonMain =
    FabButtonMainImpl(iconRes, iconRotate)