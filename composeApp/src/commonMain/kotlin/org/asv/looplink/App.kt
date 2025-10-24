package org.asv.looplink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import org.asv.looplink.components.LocalAppNavigator
import org.asv.looplink.components.LoginFields
import org.asv.looplink.network.createKtorClient
import org.asv.looplink.ui.AppNavigator
import org.asv.looplink.ui.MainScreen
import org.koin.compose.koinInject
import ui.theme.AppTheme

@Composable
fun App(
    onLoginSuccess: () -> Unit
) {
    createKtorClient()
    val isMobile = getPlatformType() == PlatformType.ANDROID
    val database: DatabaseMng = koinInject()

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
                Navigator(LoginFields(onLoginSuccess)){ nav ->
                    val appNavigator = remember{ AppNavigator(nav, null) }
                    CompositionLocalProvider(LocalAppNavigator provides appNavigator) {
                        CurrentScreen()
                    }
                }
            } else {
                Navigator(MainScreen()){ nav ->
                    val appNavigator = remember{ AppNavigator(nav, null) }
                    CompositionLocalProvider(LocalAppNavigator provides appNavigator) {
                        CurrentScreen()
                    }
                }
            }
        }
    }
}