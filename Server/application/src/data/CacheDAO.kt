package com.arpadfodor.stolenvehicledetector.data

import com.arpadfodor.stolenvehicledetector.communication.StatusCodes
import com.arpadfodor.stolenvehicledetector.model.DataTransformer
import com.arpadfodor.stolenvehicledetector.model.DataUtils
import java.lang.Exception
import java.lang.reflect.Type

open class CacheDAO<T>(name: String, typeToken: Type) : DAO<T>(name, typeToken){

    var records : MutableList<T> = readDB()

    fun initialize(){
        records = readDB()
    }

    override fun read() : MutableList<T>{
        return records
    }

    private fun readDB() : MutableList<T>{
        val content = DataUtils.getFileContent("${tablePath}${tableName}")
        return DataTransformer.jsonToType(content, typeToken) ?: mutableListOf()
    }

    override fun add(items: List<T>) : Int{
        for(item in items){
            records.add(item)
        }
        return write(records)
    }

    override fun erase() : Int{
        records.clear()
        return write(records)
    }

    override fun write(itemsToWrite: List<T>) : Int{
        try {
            val jsonContent = DataTransformer.objectToJsonString(itemsToWrite)
            DataUtils.writeFileContent("${tablePath}${tableName}", jsonContent)
        }
        catch (e: Exception){
            records = readDB()
            return StatusCodes.INTERNAL_SERVER_ERROR
        }
        return StatusCodes.SUCCESS
    }

}