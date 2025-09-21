package org.asv.looplink.operations

import androidx.compose.runtime.Composable
import org.asv.looplink.DatabaseMng
import org.asv.looplink.components.LocalDatabase
import org.asv.looplink.components.userInfo

fun logout(database: DatabaseMng){
    database.deleteUser()
    userInfo.reset()
}