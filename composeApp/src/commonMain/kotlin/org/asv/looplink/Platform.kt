package org.asv.looplink

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform