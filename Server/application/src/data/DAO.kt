package com.arpadfodor.stolenvehicledetector.data

import com.arpadfodor.stolenvehicledetector.communication.StatusCodes
import com.arpadfodor.stolenvehicledetector.data.dataclasses.LogType
import com.arpadfodor.stolenvehicledetector.model.DataTransformer
import com.arpadfodor.stolenvehicledetector.model.DataUtils
import com.arpadfodor.stolenvehicledetector.model.LoggerService
import java.lang.Exception
import java.lang.reflect.Type

open class DAO<T>(name: String, typeToken: Type){

    val tablePath = "resources/data/"
    val tableName = "${name}.json"
    val typeToken = typeToken

    open fun read() : MutableList<T>{
        val content = DataUtils.getFileContent("${tablePath}${tableName}")
        return DataTransformer.jsonToType(content, typeToken) ?: mutableListOf()
    }

    open fun add(items: List<T>) : Int{
        val records = read()
        for(item in items){
            records.add(item)
        }
        return write(records)
    }

    open fun erase() : Int{
        val records = mutableListOf<T>()
        return write(records)
    }

    open fun write(itemsToWrite: List<T>) : Int{
        try {
            val jsonContent = DataTransformer.objectToJsonString(itemsToWrite)
            DataUtils.writeFileContent("${tablePath}${tableName}", jsonContent)
        }
        catch (e: Exception){
            return StatusCodes.INTERNAL_SERVER_ERROR
        }
        return StatusCodes.SUCCESS
    }

    fun log(message: String, logType: LogType = LogType.INFO){
        LoggerService.log(this::class.simpleName?:"", message, logType)
    }

}