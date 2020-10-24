package com.arpadfodor.ktor.model.db

import com.arpadfodor.ktor.communication.StatusCodes
import com.arpadfodor.ktor.model.DataTransformer
import com.arpadfodor.ktor.model.DataUtils
import com.arpadfodor.ktor.model.dataclasses.MetaData
import com.google.gson.Gson
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type

abstract class DAO<T>(name: String, typeToken: Type, metaDAO: MetaDAO?){

    private val tablePath = "resources/data/"
    private val tableName = "$name.json"
    private val metaKey = name
    private val metaDAO = metaDAO
    val typeToken = typeToken

    var records = readDB()

    fun initialize(){
        records = readDB()
    }

    open fun read() : MutableList<T>{
        return records
    }

    private fun readDB() : MutableList<T>{
        val content = DataUtils.getFileContent("${tablePath}${tableName}")
        val data: MutableList<T> = Gson().fromJson(content, typeToken)
                ?: mutableListOf()
        return data
    }

    open fun add(element: T) : Int{
        records.add(element)
        return write(records)
    }

    open fun addMultiple(elementsToAdd: List<T>) : Int{
        for(element in elementsToAdd){
            records.add(element)
        }
        return write(records)
    }

    fun erase() : Int{
        records.clear()
        return write(records)
    }

    open fun rewrite(elementsToWrite: List<T>) : Int{
            records = elementsToWrite.toMutableList()
            return write(records)
    }

    protected fun write(elementsToWrite: List<T>) : Int{
        try {
            val jsonContent = DataTransformer.objectToJsonString(elementsToWrite)
            File("${tablePath}${tableName}").writeText(jsonContent)
            updateMeta()
        }
        catch (e: Exception){
            records = readDB()
            return StatusCodes.INTERNAL_SERVER_ERROR
        }
        return StatusCodes.SUCCESS
    }

    protected open fun updateMeta(){
        metaDAO ?: return
        val dataSize = records.size
        val modificationTimestampUTC = DataUtils.currentTimeUTC()
        metaDAO.updateMeta(MetaData(metaKey, dataSize, modificationTimestampUTC))
    }

}