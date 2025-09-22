package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import com.db.LLData
import com.db.LLDataQueries
import org.asv.looplink.components.userInfo

enum class PlatformType{
    ANDROID,
    DESKTOP
}
interface Platform {
    val name: String
}

expect fun getPlatformType(): PlatformType

expect fun getPlatform(): Platform

expect class DriverFactory {
    constructor()
    fun createDriver(): SqlDriver
}


class DatabaseMng constructor(private val driver: SqlDriver){

    fun insertIntoDatabase(name: String, uid: String){
        val dataBase = LLData(driver)
        dataBase.lLDataQueries.insert(name, uid);
    }

    fun insertUserData(
        name: String,
        uid: String,
        currentSection: String? = null,
        programCode: String? = null,
        studentContact: String? = null,
        cGPA: String? = null,
        cumail: String? = null,
        pfpImage: ByteArray
    ){
        val database = LLData(driver)
        database.lLDataQueries.insertAll(
            name,
            uid,
            currentSection,
            programCode,
            studentContact,
            cGPA,
            cumail,
            pfpImage
        )
    }

    fun getProfileImage(): ByteArray{
        val database = LLData(driver)
        return database.lLDataQueries.getPFP(){it!!}.executeAsOne()
    }

    fun getAllFromDatabase(): List<List<String>>{
        val database = LLData(driver)
        val userInfo = database.lLDataQueries.selectAll().executeAsList()

        return userInfo.map { listOf(
            it.name,
            it.uid,
            it.section?:"null",
            it.program?:"null",
            it.contact?:"null",
            it.cGPA?:"null",
            it.email?:"null",
        )
        }
    }

    fun getUserData(): userInfo {
        val database = LLData(driver)
        val userData = database.lLDataQueries.selectAll().executeAsList()

        return userData.map {
            userInfo.apply {
                name = it.name
                uid = it.uid
                section = it.section
                program = it.program
                contact = it.contact
                cGPA = it.cGPA
                email = it.email
                pfpImage = it.pfpImage != null
            }
        }[0]
    }


    fun deleteUser(){
        val database = LLData(driver)
        database.lLDataQueries.delete()
    }

    fun getSize(): Int{
        val database = LLData(driver)
        return database.lLDataQueries.getSize().executeAsOne().toInt()
    }
}


