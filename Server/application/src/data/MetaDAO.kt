package com.arpadfodor.ktor.data

import com.arpadfodor.ktor.data.dataclasses.MetaData
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<MetaData>>() {}.type

class MetaDAO(name: String) : CacheDAO<MetaData>(name, typeToken){

    fun updateMeta(meta: MetaData){
        val updatedMeta = records.filter { it.tableId != meta.tableId }.toMutableList()
        records = updatedMeta
        records.add(meta)
        write(records)
    }

    fun getMeta(tableId: String) : MetaData {
        for(element in records){
            if(element.tableId == tableId){
                return element
            }
        }
        return MetaData("", 0, "never")
    }

    fun getSize(tableId: String) : Int{
        for(element in records){
            if(element.tableId == tableId){
                return element.dataSize
            }
        }
        return 0
    }

    fun getModificationTimestamp(tableId: String) : String{
        for(element in records){
            if(element.tableId == tableId){
                return element.modificationTimeStampUTC
            }
        }
        return ""
    }

}