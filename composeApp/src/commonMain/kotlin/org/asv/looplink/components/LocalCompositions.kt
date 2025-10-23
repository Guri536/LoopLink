package org.asv.looplink.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.asv.looplink.DatabaseMng
import org.asv.looplink.MainViewModel
import org.asv.looplink.ui.AppNavigator
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI
val LocalAppNavigator = staticCompositionLocalOf<AppNavigator?> { null }

val LocalPeerDiscoveryViewModel = staticCompositionLocalOf<PeerDiscoveryViewModel?> { null }