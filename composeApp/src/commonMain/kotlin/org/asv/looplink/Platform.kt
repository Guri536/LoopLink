package org.asv.looplink

import app.cash.sqldelight.db.SqlDriver
import com.db.LLData
import com.db.LLDataQueries

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class DriverFactory{
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): LLData {
    val driver = driverFactory.createDriver()
    val database = LLData(driver)
    return database
}

suspend fun insertIntoDatabase(driver: SqlDriver, name: String, uid: String){
    val dataBase = LLData(driver)
    dataBase.lLDataQueries.insert(name, uid);
}

fun getAllFromDatabase(driver: SqlDriver){
    val database = LLData(driver)
    val userInfo = database.lLDataQueries.selectAll()

    println(userInfo)
}