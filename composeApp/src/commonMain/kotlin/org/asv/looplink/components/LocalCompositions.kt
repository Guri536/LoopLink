package org.asv.looplink.components

import androidx.compose.runtime.staticCompositionLocalOf
import org.asv.looplink.DatabaseMng
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI

val LocalDatabase = staticCompositionLocalOf<DatabaseMng> {
    error("No Database")
}

val LocalCuimsApi = staticCompositionLocalOf<cuimsAPI> {
    error("No API")
}

val LocalPeerDiscoveryViewModel = staticCompositionLocalOf<PeerDiscoveryViewModel?> { null }
