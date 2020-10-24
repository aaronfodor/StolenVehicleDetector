package com.arpadfodor.ktor.model.db

import com.arpadfodor.ktor.model.dataclasses.MetaData
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<MetaData>>() {}.type

class MetaDAO(name: String) : DAO<MetaData>(name, typeToken, null){

    fun updateMeta(meta: MetaData){
        val updatedMeta = records.filter { it.Id != meta.Id }.toMutableList()
        records = updatedMeta
        records.add(meta)
        write(records)
    }

    fun getMeta(key: String) : MetaData{
        for(element in records){
            if(element.Id == key){
                return element
            }
        }
        return MetaData("", 0, "never")
    }

    fun getSize(key: String) : Int{
        for(element in records){
            if(element.Id == key){
                return element.dataSize
            }
        }
        return 0
    }

    fun getModificationTimestamp(key: String) : String{
        for(element in records){
            if(element.Id == key){
                return element.modificationTimeStampUTC
            }
        }
        return ""
    }

    override fun updateMeta(){
        return
    }

}