package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VehicleCoordinatesDatabase{

    val dbPath = "resources/"
    val dbName = "vehicle_coordinates.json"

    fun read() : VehicleCoordinates{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val vehicleCoordinates: VehicleCoordinates = Gson().fromJson(content, object : TypeToken<VehicleCoordinates>() {}.type)
                ?: VehicleCoordinates(mutableListOf(), MetaData(0, "0"))
        return vehicleCoordinates
    }

    fun add(vehicleCoordinate: VehicleCoordinate) : Boolean{

        val content = read()

        var isVehicleAlreadyInDatabase = false
        var isModified = false

        for(element in content.coordinates){

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
            content.coordinates.add(vehicleCoordinate)
            isModified = true
        }

        if(isModified){
            content.meta.modificationTimeStampUTC = DataUtils.currentTimeUTC()
            content.meta.dataSize = content.coordinates.size
            val dbContent = DataTransformer.transformObjectToString(content)
            File("${dbPath}${dbName}").writeText(dbContent)
        }

        return isModified

    }

}