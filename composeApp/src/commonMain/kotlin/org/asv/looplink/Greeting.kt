package org.asv.looplink

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello lets see how this works, ${platform.name}!"
    }
}