package org.asv.looplink.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.asv.looplink.DatabaseMng
import org.asv.looplink.MainViewModel
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI

val LocalDatabase = staticCompositionLocalOf<DatabaseMng> {
    error("No Database")
}

val LocalCuimsApi = staticCompositionLocalOf<cuimsAPI> {
    error("No API")
}

val LocalPeerDiscoveryViewModel = staticCompositionLocalOf<PeerDiscoveryViewModel?> { null }

val LocalTabNavigator = staticCompositionLocalOf<TabNavigator?> { null }

val LocalMainNavigator = staticCompositionLocalOf<Navigator?> { null }

val LocalChatViewModel = staticCompositionLocalOf<ChatViewModel?>{ null }

val LocalMainViewModel = compositionLocalOf<MainViewModel?> { null }