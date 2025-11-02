package org.asv.looplink.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.asv.looplink.DatabaseManager
import org.asv.looplink.components.chat.User
import org.asv.looplink.data.model.UserModel

class UserRepository(
    private val database: DatabaseManager,
    private val coroutineScope: CoroutineScope // e.g., Injected by Koin
) {
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _knownUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    val knownUsers = _knownUsers.asStateFlow()

    private val _currentUserPort = MutableStateFlow(0)
    val currentUserPort = _currentUserPort.asStateFlow()

    init {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val peersFromDb = database.getAllPeers()
                println("Loading Users: ${peersFromDb}")
                _knownUsers.update { it ->
                    peersFromDb.associateBy { it.id }
                }
                println("UserRepository: Loaded ${peersFromDb.size} peers from DB.")
            } catch (e: Exception) {
                println("UserRepository: Error loading peers: ${e.message}")
            }
        }
    }

    fun loadUser() {
        if (database.isLoggedIn() > 0) {
            val userInfo = database.getUserData()
            _currentUser.value = UserModel(
                name = userInfo.name,
                uid = userInfo.uid,
                section = userInfo.section,
                program = userInfo.program,
                contact = userInfo.contact,
                cGPA = userInfo.cGPA,
                email = userInfo.email,
                pfpPath = userInfo.pfpPath
            )
            _knownUsers.update { currentUsers ->
                currentUsers + (userInfo.uid to User(
                    id = userInfo.uid,
                    name = userInfo.name,
                    pfpPath = userInfo.pfpPath
                ))
            }
        }

    }

    fun getUserIdAndName(): Pair<String?, String?> =
        Pair(_currentUser.value?.uid, _currentUser.value?.name)

    fun insertAndLoadUser(it: UserModel) {
        database.insertUserData(
            it.name,
            it.uid,
            it.section,
            it.program,
            it.contact,
            it.cGPA,
            it.email,
            it.pfpPath!!
        )

        _currentUser.value = it
    }


    fun logout() {
        _currentUser.value = null
    }

    fun getCurrentUser(): User {
        return User(_currentUser.value?.uid!!, _currentUser.value?.name!!)
    }

    fun getUserById(userId: String): User? {
        return _knownUsers.value[userId]
    }

    fun getUserName(userId: String): String? {
        return _knownUsers.value[userId]?.name
    }

    fun getUserpfpPath(userId: String): String? {
        return _knownUsers.value[userId]?.pfpPath
    }

    fun addUserToCache(user: User) {
        _knownUsers.update { currentUsers ->
            if (currentUsers.containsKey(user.id))
                return@update currentUsers + (user.id to currentUsers[user.id]!!.copy(
                    hostAddress = user.hostAddress,
                    port = user.port
                ))
            coroutineScope.launch {
                database.savePeer(user)
            }
            currentUsers + (user.id to user)
        }
    }

    fun setCurrentUserPfp(path: String) {
        _currentUser.update {
            coroutineScope.launch {
                database.updateLocalUserPfp(path)
            }
            _currentUser.value?.copy(pfpPath = path)
        }
    }

    fun updateUserPfpPath(userId: String, pfpPath: String) {
        _knownUsers.update { currUsers ->
            val user = currUsers[userId]
            if (user != null) {
//                println("Updating $userId's pfp to $pfpPath")
                coroutineScope.launch {
                    database.updatePeerPfpPath(userId, pfpPath)
                    val users = database.getPeer(userId)
//                    println("Updated user data: $users")
                }
                currUsers + (userId to user.copy(pfpPath = pfpPath))
            } else {
                currUsers
            }
        }
    }

    fun setCurrentUsrPort(port: Int) {
        _currentUserPort.value = port
    }
}