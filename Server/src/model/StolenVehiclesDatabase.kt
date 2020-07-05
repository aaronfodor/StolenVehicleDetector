package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object StolenVehiclesDatabase{

    val dbPath = "resources/"
    val dbName = "stolen_vehicles.json"

    fun readRawListFrom(rawFilePath: String) : MutableList<StolenVehicle>{
        val content = DataUtils.getFileContent("${dbPath}${rawFilePath}")
        val stolenVehiclesList: MutableList<StolenVehicle> = Gson().fromJson(content, object : TypeToken<MutableList<StolenVehicle>>() {}.type)
                ?: mutableListOf()
        return stolenVehiclesList
    }

    fun read() : StolenVehicles{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val stolenVehicles: StolenVehicles = Gson().fromJson(content, object : TypeToken<StolenVehicles>() {}.type)
                ?: StolenVehicles(mutableListOf(), MetaData(0, "0"))
        return stolenVehicles
    }

    fun add(stolenVehicle: StolenVehicle){
        val content = read()
        content.vehicles.add(stolenVehicle)
        content.meta.modificationTimeStampUTC = DataUtils.currentTimeUTC()
        content.meta.dataSize = content.vehicles.size
        val dbContent = DataTransformer.transformObjectToString(content)
        File("${dbPath}${dbName}").writeText(dbContent)
    }

    fun write(stolenVehicles: StolenVehicles){
        val jsonContent = DataTransformer.transformObjectToString(stolenVehicles)
        File("${dbPath}${dbName}").writeText(jsonContent)
    }

}