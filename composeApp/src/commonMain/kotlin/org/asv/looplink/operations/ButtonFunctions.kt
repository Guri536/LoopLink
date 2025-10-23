package org.asv.looplink.operations

import androidx.compose.runtime.collectAsState
import org.asv.looplink.viewmodel.ChatViewModel
import org.koin.java.KoinJavaComponent.get

fun addRoom(){
    val chatViewModel = get<ChatViewModel>(ChatViewModel::class.java)
    val rooms = chatViewModel.rooms.value
}