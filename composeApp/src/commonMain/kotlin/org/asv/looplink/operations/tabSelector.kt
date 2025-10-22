package org.asv.looplink.operations

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.asv.looplink.components.LocalTabNavigator

@Composable
fun NavMain(screen: Screen) = getMainNav()?.push(screen)


@Composable
fun PushToNavigator(isWideSceren: Boolean, screen: Screen, tab: Tab) {
    val currNav = getMainNav()
    if (isWideSceren) {
        LocalTabNavigator.current?.current = tab
    } else {
        currNav?.push(screen)
    }
}

fun PushToNavigator(
    isWideSceren: Boolean,
    screen: Screen,
    tab: Tab,
    navigator: Navigator?,
    tabNavigator: TabNavigator?
) {
    if (isWideSceren) {
        tabNavigator?.current = tab
    } else {
        navigator?.push(screen)
    }
}

@Composable
fun getMainNav(): Navigator? {
    val tabN = LocalTabNavigator.current
    return if (tabN == null) LocalNavigator.currentOrThrow else LocalNavigator.current?.parent
}