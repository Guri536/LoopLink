package org.asv.looplink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import org.asv.looplink.components.LocalCuimsApi
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LocalPeerDiscoveryViewModel
import org.asv.looplink.components.LoginFields
import org.asv.looplink.components.SettingsPage
import org.asv.looplink.network.createKtorClient // Keep for now, though sendMessage is unused in this snippet
import org.asv.looplink.theme.Colors
import org.asv.looplink.ui.MainScreen
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.asv.looplink.webDriver.cuimsAPI
import ui.theme.AppTheme


@Composable
fun App(
    database: DatabaseMng,
    cuimsAPI: cuimsAPI,
    peerDiscoveryViewModel: PeerDiscoveryViewModel
) {
    createKtorClient()

    CompositionLocalProvider(
        LocalDatabase provides database,
        LocalCuimsApi provides cuimsAPI,
        LocalPeerDiscoveryViewModel provides peerDiscoveryViewModel
    ) {
        AppTheme {
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (database.getSize() == 0) {
                    Navigator(LoginFields())
                } else {
                    Navigator(MainScreen())
                }
            }
        }
    }
}