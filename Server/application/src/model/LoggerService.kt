package com.arpadfodor.stolenvehicledetector.model

import com.arpadfodor.stolenvehicledetector.data.DAO
import com.arpadfodor.stolenvehicledetector.data.dataclasses.Log
import com.arpadfodor.stolenvehicledetector.data.dataclasses.LogType
import com.google.gson.reflect.TypeToken

object LoggerService{

    private val name = "log"
    private var logId = 1
    var logToConsole = true
    private val typeToken = object : TypeToken<MutableList<Log>>() {}.type
    private val logDAO = DAO<Log>(name, typeToken)

    fun log(summary: String, message: String, type: LogType = LogType.INFO){

        val currentTimeUTC = DataUtils.currentTimeUTC()
        val log = Log(logId, currentTimeUTC, type, summary, message)
        logDAO.add(listOf(log))
        logId += 1

        if(logToConsole){
            logToConsole(log)
        }

    }

    private fun logToConsole(log: Log){
        println("[${log.timestampUTC}] ${log.type} - ${log.summary}: ${log.message}")
    }

}