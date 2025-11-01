package org.asv.looplink.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.asv.looplink.components.chat.LoopLinkEvent
import org.asv.looplink.components.chat.Message
import org.asv.looplink.components.chat.TypingEvent

/**
 * A custom SerializersModule that teaches kotlinx.serialization how to handle
 * the LoopLinkEvent sealed interface and its subclasses.
 */
private val loopLinkSerializersModule = SerializersModule {
    polymorphic(LoopLinkEvent::class) {
        subclass(Message::class)
        subclass(TypingEvent::class)
    }
}

/**
 * The single, app-wide Json instance configured for polymorphism.
 * Use this for ALL serialization/deserialization.
 */
val AppJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    serializersModule = loopLinkSerializersModule // This is the important part
}