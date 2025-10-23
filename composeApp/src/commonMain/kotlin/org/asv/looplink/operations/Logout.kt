package org.asv.looplink.operations

import org.koin.java.KoinJavaComponent.get
import org.asv.looplink.DatabaseMng
import org.asv.looplink.MainViewModel
import org.asv.looplink.components.userInfo

fun logout(database: DatabaseMng){
    get<MainViewModel>(MainViewModel::class.java).stopP2PServices()
    database.deleteUser()
    userInfo.reset()
}