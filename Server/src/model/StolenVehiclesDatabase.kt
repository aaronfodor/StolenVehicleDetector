package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object StolenVehiclesDatabase{

    val dbPath = "resources/"
    val dbName = "stolen_vehicles.json"

    var stolenVehicles = read()

    fun initialize(){
        stolenVehicles = read()
    }

    fun read() : StolenVehicles{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val stolenVehicles: StolenVehicles = Gson().fromJson(content, object : TypeToken<StolenVehicles>() {}.type)
                ?: StolenVehicles(mutableListOf(), MetaData(0, "0"))
        return stolenVehicles
    }

    fun add(stolenVehicle: StolenVehicle){
        stolenVehicles = read()
        stolenVehicles.vehicles.add(stolenVehicle)
        stolenVehicles.meta = updateMeta(stolenVehicles.vehicles)
        write(stolenVehicles)
    }

    private fun addMultiple(vehicles: List<StolenVehicle>){
        stolenVehicles = read()
        for(element in vehicles){
            stolenVehicles.vehicles.add(element)
        }
        stolenVehicles.meta = updateMeta(stolenVehicles.vehicles)
        write(stolenVehicles)
    }

    fun erase(){
        stolenVehicles = read()
        stolenVehicles.vehicles.clear()
        stolenVehicles.meta = updateMeta(stolenVehicles.vehicles)
        write(stolenVehicles)
    }

    fun rewrite(stolenVehiclesList: MutableList<StolenVehicle>){
        erase()
        val meta = updateMeta(stolenVehiclesList)
        stolenVehicles = StolenVehicles(stolenVehiclesList, meta)
        write(stolenVehicles)
    }

    private fun write(stolenVehicles: StolenVehicles){
        val jsonContent = DataTransformer.objectToJsonString(stolenVehicles)
        File("${dbPath}${dbName}").writeText(jsonContent)
    }

    private fun updateMeta(stolenVehicles: MutableList<StolenVehicle>) : MetaData{
        val dataSize = stolenVehicles.size
        val modificationTimeStampUTC = DataUtils.currentTimeUTC()
        return MetaData(dataSize, modificationTimeStampUTC)
    }

}