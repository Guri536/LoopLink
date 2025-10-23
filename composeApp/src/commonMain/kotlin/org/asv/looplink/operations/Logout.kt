package org.asv.looplink.operations

import org.koin.java.KoinJavaComponent.get
import org.asv.looplink.DatabaseMng
import org.asv.looplink.MainViewModel
import org.asv.looplink.data.repository.UserRespository

fun logout(){
    get<MainViewModel>(MainViewModel::class.java).stopP2PServices()
    get<DatabaseMng>(DatabaseMng::class.java).deleteUser()
    get<UserRespository>(UserRespository::class.java).logout()
}