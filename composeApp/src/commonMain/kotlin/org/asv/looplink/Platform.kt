package org.asv.looplink

import androidx.compose.ui.text.capitalize
import app.cash.sqldelight.db.SqlDriver
import com.db.LLData
import com.db.LoopLinkUser
import java.util.Locale

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
    fun createDriver(): SqlDriver
}


class DatabaseMng constructor(private val driver: SqlDriver){
    val dataBase = LLData(driver)

    fun insertIntoDatabase(name: String, uid: String){
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
        pfpPath: String? = null
    ){
        val database = LLData(driver)
        database.lLDataQueries.insertAll(
            name.lowercase().capitalize(Locale.UK),
            uid,
            currentSection,
            programCode,
            studentContact,
            cGPA,
            cumail,
            pfpPath
        )
    }

    fun getUserData(): LoopLinkUser {
        val database = LLData(driver)
        return database.lLDataQueries.selectAll().executeAsList()[0]
    }

    fun deleteUser(){
        val database = LLData(driver)
        database.lLDataQueries.delete()
    }

    fun getSize(): Int{
        val database = LLData(driver)
        return database.lLDataQueries.getSize().executeAsOne().toInt()
    }

    fun getPfpImagePath(): String? {
        return dataBase.lLDataQueries.getPfpPath(){it!!}.executeAsOne()
    }
}


