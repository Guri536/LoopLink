package org.asv.looplink.secrets

actual object APIKeys {
    actual val ocrKey: String = System.getenv("ocrSpaceAPIKEY")?: ""
}