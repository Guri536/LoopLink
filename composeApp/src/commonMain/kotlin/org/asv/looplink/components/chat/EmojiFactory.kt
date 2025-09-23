package org.asv.looplink.components.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import ui.theme.resource
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EmojiFont() = FontFamily(
    resource("font/noto_emoji_light", FontWeight.Light),
    resource("font/noto_emoji_regular", FontWeight.Normal),
    resource("font/noto_emoji_med", FontWeight.Medium)
)

//private val V.value: Any
val testEmojis =
    listOf("🍇", "🍅", "🥬", "🍞", "🧀", "🥚", "🥩", "🍫", "🍕", "🍷", "🧃", "🧼", "🧻", "🧴", "🍏")


// For Android. Forgot why I had to do this 🙃
//actual fun getPlatformTextStyle(): PlatformTextStyle = PlatformTextStyle(
//    emojiSupportMatch = EmojiSupportMatch.None
//)
expect fun getPlatformTextStyle(): PlatformTextStyle

/**
 * Draws concentric rings of emojis around the center of the viewport.
 */
@Composable
fun EmojiBg(
    modifier: Modifier = Modifier,
    emojiBgState: EmojiBgState = rememberEmojiBgState(emojis = testEmojis),
    emojiSize: Dp,
    emojiColor: Color,
    gap: Dp,
) {
    val tm = rememberTextMeasurer()
    val density = LocalDensity.current
    val font = EmojiFont()

    val textSize = density.run {
        ((emojiSize / 2) * sqrt(2f)).toSp() // square in circle
    }

    val textStyle = TextStyle(
        fontSize = textSize,
        fontFamily = font,
        color = emojiColor,
        platformStyle = getPlatformTextStyle()
    )

    emojiBgState.update(
        itemDiameterPx = density.run { (emojiSize + gap / 2).toPx() }.toInt(),
        textSizePx = density.run { emojiSize.toPx() }.toInt()
    )

    Box(
        modifier
            .fillMaxSize()
            .onSizeChanged() { it ->
                emojiBgState.updateContainerSize(
                    containerSize = it
                )
            }
            .drawWithCache {
                val center = size.div(2f)

                onDrawBehind {
                    emojiBgState.items.forEach { item ->
                        val rotAnimatable = emojiBgState.getRotAnimatable(item.layer)
                        val scaleAnimatable = emojiBgState.getScaleAnimatable(item.layer)

                        val left = item.pos.x.toFloat()
                        val top = item.pos.y.toFloat()

                        withTransform({
                            translate(
                                left = left + center.width,
                                top = top + center.height,
                            )

                            rotate(
                                degrees = item.emojiPointer * 87f + rotAnimatable.value,
                                pivot = item.center
                            )

                            scale(
                                scaleX = scaleAnimatable.value,
                                scaleY = scaleAnimatable.value,
                                pivot = item.center
                            )
                        }) {
                            drawText(
                                tm,
                                emojiBgState.getEmojiForPointer(item.emojiPointer),
                                style = textStyle
                            )
                        }
                    }
                }
            }
    )
}

@Composable
fun rememberEmojiBgState(
    emojis: List<String>,
): EmojiBgState {
    val scope = rememberCoroutineScope()
    return remember {
        EmojiBgState(
            scope = scope,
            initialEmojis = emojis
        )
    }
}

@Stable
class EmojiBgState(
    val scope: CoroutineScope,
    initialEmojis: List<String>,
) {
    private var emojis by mutableStateOf(initialEmojis)

    private var itemDiameterPx by mutableIntStateOf(0)
    private var textSizePx by mutableIntStateOf(0)
    private var containerSize by mutableStateOf(IntSize.Zero)

    /**
     * Number of concentric rings around the center. Updates when container or emoji size changes.
     */
    private val layerCount by derivedStateOf(structuralEqualityPolicy()) {
        ceil(
            (sqrt(
                containerSize.width
                    .toDouble()
                    .pow(2.0) + containerSize.height
                    .toDouble()
                    .pow(2.0)
            ) / 2) / itemDiameterPx
        ).toInt() + 1
    }

    /**
     * Given [layerCount], lays out all items. Ignores container size changes as long as [layerCount] doesn't change.
     */
    private val allItems by derivedStateOf {
        val itemCenter = IntOffset(textSizePx / 2, textSizePx / 2)

        buildList {
            var emojiPointer = 0

            for (layer in 0 until layerCount) {
                val itemsInLayer = (layer * 6).coerceAtLeast(1)
                for (indexInLayer in 0 until itemsInLayer) {
                    val angle = 2 * PI * indexInLayer / itemsInLayer + 8 * layer
                    val distance = layer * itemDiameterPx
                    val x = (distance * cos(angle)).toInt() - itemCenter.x
                    val y = (distance * sin(angle)).toInt() - itemCenter.y

                    // Emoji is picked by taking the next emoji from this.emojis. Since items are filtered later on,
                    // we have to assign a fix number to every item, so that it always picks the same emoji, even when one or more
                    // predecessor are filtered out.
                    val currentPointer = emojiPointer++

                    Item(
                        emojiPointer = currentPointer,
                        pos = IntOffset(x, y),
                        layer = layer,
                        indexInLayer = indexInLayer,
                        size = IntSize(textSizePx, textSizePx)
                    ).also { add(it) }
                }
            }
        }
    }

    /**
     * Contains only items that are currently visible.
     */
    val items: List<Item>
        get() {
            return allItems.filter {
                (-itemDiameterPx..containerSize.width).contains(containerSize.width / 2 + it.pos.x) &&
                        (-itemDiameterPx..containerSize.height).contains(containerSize.height / 2 + it.pos.y)
            }
        }

    /**
     * For animations
     */
    private val layerToPointers: Map<Int, List<Int>>
        get() = allItems.groupBy { it.layer }.mapValues { it.value.map { it.emojiPointer } }

    /**
     * Caches the shown emoji for a given pointer. When changing emojis,
     * the cached emoji is removed and since [getEmojiForPointer] is observed, creates the next
     * emoji right away. Makes it possible to change the emojis in a wave, layer by layer
     */
    private val pointerToEmoji = mutableStateMapOf<Int, String>()
    fun getEmojiForPointer(pointer: Int) = pointerToEmoji.getOrPut(pointer) {
        emojis.size.takeIf { it > 0 }?.let { emojis[pointer % it] } ?: ""
    }

    private val rotAnimatables = mutableMapOf<Int, Animatable<Float, AnimationVector1D>>()
    fun getRotAnimatable(layer: Int) = rotAnimatables.getOrPut(layer) { Animatable(1f) }

    private val scaleAnimatables = mutableMapOf<Int, Animatable<Float, AnimationVector1D>>()
    fun getScaleAnimatable(layer: Int) = scaleAnimatables.getOrPut(layer) { Animatable(1f) }

    fun updateContainerSize(
        containerSize: IntSize,
    ) {
        this.containerSize = containerSize
    }

    fun update(
        itemDiameterPx: Int,
        textSizePx: Int
    ) {
        this.itemDiameterPx = itemDiameterPx
        this.textSizePx = textSizePx
    }

    fun newEmojis(emojis: List<String>) {
        if (this.emojis == emojis) return

        this.emojis = emojis
        scope.launch {
            wave(
                switchEmojiNowForLayer = { layer ->
                    // Remove the currently shown emoji for the given layer
                    layerToPointers[layer]?.let { pointers ->
                        pointers.forEach { pointer ->
                            // Remove the currently shown emoji for the given pointer.
                            pointerToEmoji.remove(pointer)
                        }
                    }
                }
            )
        }
    }

    fun shake() = scope.launch {
        rotAnimatables.forEach { (layer, animatable) ->
            launch {
                delay(layer * 100L)
                while (true) {
                    animatable.animateTo(-10f, tween(150, easing = LinearEasing))
                    animatable.animateTo(10f, tween(150, easing = LinearEasing))
                }
            }
        }
    }

    fun wave(switchEmojiNowForLayer: (Int) -> Unit) = scope.launch {
        rotAnimatables.forEach { (layer, animatable) ->
            launch {
                delay(layer * 80L)
                animatable.snapTo(30f)
                animatable.animateTo(
                    1f,
                    spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMediumLow)
                )
            }
        }

        scaleAnimatables.forEach { (layer, animatable) ->
            launch {
                delay(layer * 80L)
                animatable.snapTo(1.3f)
                switchEmojiNowForLayer(layer)
                animatable.animateTo(
                    1f,
                    spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMediumLow)
                )
            }
        }
    }

    fun stopAnimations() = scope.launch {
        rotAnimatables.values.forEachIndexed { layer, animatable ->
            launch {
                delay(layer * 80L)
                animatable.animateTo(1f)
            }
        }

        scaleAnimatables.values.forEachIndexed { layer, animatable ->
            launch {
                delay(layer * 80L)
                animatable.animateTo(1f)
            }
        }
    }

    data class Item(
        val emojiPointer: Int,
        val layer: Int,
        val indexInLayer: Int,
        val pos: IntOffset,
        val size: IntSize
    ) {
        val center = Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )
    }
}