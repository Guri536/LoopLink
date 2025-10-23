package org.asv.looplink.ui

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.asv.looplink.components.SettingsPage
import org.asv.looplink.viewmodel.RoomItem

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

    fun navigateToChat(room: RoomItem){
        if(tabNavigator != null){
            tabNavigator.current = ChatTab(room)
        } else {
            navigator.push(ChatTabScreen(room))
        }
    }

    fun pushScreen(screen: Screen){
        navigator.push(screen)
    }
}