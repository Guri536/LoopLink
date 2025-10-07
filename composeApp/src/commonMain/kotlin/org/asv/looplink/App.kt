package org.asv.looplink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LocalMainViewModel
import org.asv.looplink.components.LoginFields
import org.asv.looplink.components.loadUserInfo
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.ui.MainScreen
import ui.theme.AppTheme

expect class MainViewModel {
    fun startP2PServices()
    fun stopP2PServices()
}

@Composable
fun App(
    onLoginSuccess: () -> Unit
) {
    createKtorClient()
    val isMobile = getPlatformType() == PlatformType.ANDROID
    val database = LocalDatabase.current

    AppTheme {
        Column(
            modifier =
                if (isMobile) {
                    Modifier
                        .background(Color.Black)
                        .displayCutoutPadding()
                        .fillMaxSize()
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .safeContentPadding()
                        .fillMaxSize()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (database.getSize() == 0) {
                Navigator(LoginFields(onLoginSuccess))
            } else {
                loadUserInfo(database)
                val mainViewModel = LocalMainViewModel.currentOrThrow
                mainViewModel.startP2PServices()

                Navigator(MainScreen())
            }

        }
    }
}