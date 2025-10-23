package org.asv.looplink.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.asv.looplink.DatabaseMng
import org.asv.looplink.components.chat.User
import org.asv.looplink.data.model.UserModel
import org.koin.java.KoinJavaComponent.get

class UserRespository {
    val database: DatabaseMng = get<DatabaseMng>(DatabaseMng::class.java)
    private val _curentUser = MutableStateFlow<UserModel?>(null)
    val currentUser = _curentUser.asStateFlow()

    fun loadUser(){
        if(database.getSize() > 0){
            val userInfo = database.getUserData()
            _curentUser.value = UserModel(
                name = userInfo.name,
                uid = userInfo.uid,
                section = userInfo.section,
                program = userInfo.program,
                contact = userInfo.contact,
                cGPA = userInfo.cGPA,
                email = userInfo.email,
                picture = userInfo.pfpImage
            )
        }
    }

    fun insertAndLoadUser(it: UserModel){
        database.insertUserData(
            it.name,
            it.uid,
            it.section,
            it.program,
            it.contact,
            it.cGPA,
            it.email,
            it.picture!!
        )

        _curentUser.value = it
    }


    fun logout(){
        _curentUser.value = null
    }

    fun getUser(): User {
        return User(_curentUser.value?.name!!, _curentUser.value?.picture)
    }
}