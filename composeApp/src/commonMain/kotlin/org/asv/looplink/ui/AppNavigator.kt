package org.asv.looplink.ui

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.asv.looplink.components.SettingsPage

class AppNavigator(
    val navigator: Navigator,
    val tabNavigator: TabNavigator?
) {
    fun push(screen: Screen, tab: Tab) {
        if (tabNavigator != null) {
            tabNavigator.current = tab
        } else {
            navigator.push(screen)
        }
    }

    fun pop() {
        if (tabNavigator != null) {
            tabNavigator.current = EmptyChatTab
        } else {
            navigator.pop()
        }
    }

    fun navigateToSettings(){
        navigator.push(SettingsPage())
    }

    fun navigateToChat(roomId: Int){
        if(tabNavigator != null){
            tabNavigator.current = ChatTab(roomId)
        } else {
            navigator.push(ChatTabScreen(roomId))
        }
    }

    fun pushScreen(screen: Screen){
        navigator.push(screen)
    }
}