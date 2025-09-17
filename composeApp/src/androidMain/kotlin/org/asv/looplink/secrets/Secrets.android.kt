package org.asv.looplink.secrets

import org.asv.looplink.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.util.Properties


actual object APIKeys {
    actual val ocrKey: String = BuildConfig.ocrSpaceAPIKEY
}
