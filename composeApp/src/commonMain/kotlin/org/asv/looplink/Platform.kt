package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import com.db.LLData
import com.db.LLDataQueries

interface Platform {
    val name: String
}

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

    fun getAllFromDatabase(): List<List<String>>{
        val database = LLData(driver)
        val userInfo = database.lLDataQueries.selectAll().executeAsList()

//        println("User Info: $userInfo")
        return userInfo.map { listOf(it.name, it.uid) }
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


