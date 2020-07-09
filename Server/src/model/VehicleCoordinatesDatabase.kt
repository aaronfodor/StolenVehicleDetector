package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VehicleCoordinatesDatabase{

    val dbPath = "resources/"
    val dbName = "vehicle_coordinates.json"

    var vehicleCoordinates = read()

    fun initialize(){
        vehicleCoordinates = read()
    }

    fun read() : VehicleCoordinates{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val vehicleCoordinates: VehicleCoordinates = Gson().fromJson(content, object : TypeToken<VehicleCoordinates>() {}.type)
                ?: VehicleCoordinates(mutableListOf(), MetaData(0, "0"))
        return vehicleCoordinates
    }

    fun add(vehicleCoordinate: VehicleCoordinate) : Boolean{

        vehicleCoordinates = read()

        var isVehicleAlreadyInDatabase = false
        var isModified = false

        for(element in vehicleCoordinates.coordinates){

            if(element.vehicleLicensePlate == vehicleCoordinate.vehicleLicensePlate){

                val currentTimeStamp = DataTransformer.stringToDate(element.detectionTimeStampUTC)
                val newTimeStamp = DataTransformer.stringToDate(vehicleCoordinate.detectionTimeStampUTC)

                if(newTimeStamp.after(currentTimeStamp)){
                    element.latitude = vehicleCoordinate.latitude
                    element.longitude = vehicleCoordinate.longitude
                    element.detectionTimeStampUTC = vehicleCoordinate.detectionTimeStampUTC
                }

                isVehicleAlreadyInDatabase = true
                isModified = true
                break
            }

        }

        if(!isVehicleAlreadyInDatabase){
            vehicleCoordinates.coordinates.add(vehicleCoordinate)
            isModified = true
        }

        if(isModified){
            vehicleCoordinates.meta = updateMeta(vehicleCoordinates.coordinates)
            write(vehicleCoordinates)
        }

        return isModified

    }

    fun erase(){
        vehicleCoordinates = read()
        vehicleCoordinates.coordinates.clear()
        vehicleCoordinates.meta = updateMeta(vehicleCoordinates.coordinates)
        write(vehicleCoordinates)
    }

    private fun write(vehicleCoordinates: VehicleCoordinates){
        val dbContent = DataTransformer.objectToJsonString(vehicleCoordinates)
        File("${dbPath}${dbName}").writeText(dbContent)
    }

    private fun updateMeta(vehicleCoordinates: MutableList<VehicleCoordinate>) : MetaData{
        val dataSize = vehicleCoordinates.size
        val modificationTimeStampUTC = DataUtils.currentTimeUTC()
        return MetaData(dataSize, modificationTimeStampUTC)
    }

}