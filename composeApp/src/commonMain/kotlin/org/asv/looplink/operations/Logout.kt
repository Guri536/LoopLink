package org.asv.looplink.operations

import androidx.compose.runtime.Composable
import org.asv.looplink.DatabaseMng
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.LocalMainViewModel
import org.asv.looplink.components.LocalPeerDiscoveryViewModel
import org.asv.looplink.components.userInfo

fun logout(database: DatabaseMng){
//    LocalPeerDiscoveryViewModel.current?.stopDiscovery()
//    LocalMainViewModel.current?.stopP2PServices()
    database.deleteUser()
    userInfo.reset()
}