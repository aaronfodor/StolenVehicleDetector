package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.StatusCodes
import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<VehicleReport>>() {}.type

class CurrentsDAO(name: String, metaDAO: MetaDAO?) : DAO<VehicleReport>(name, typeToken, metaDAO){

    override fun add(currentVehicleData: VehicleReport) : Int{

        var isVehicleAlreadyInDatabase = false
        var isModified = false

        for(element in records){

            if(element.Vehicle == currentVehicleData.Vehicle){

                val currentTimeStamp = DataTransformer.stringToDate(element.timestampUTC)
                val newTimeStamp = DataTransformer.stringToDate(currentVehicleData.timestampUTC)

                if(newTimeStamp.after(currentTimeStamp)){
                    element.latitude = currentVehicleData.latitude
                    element.longitude = currentVehicleData.longitude
                    element.timestampUTC = currentVehicleData.timestampUTC
                }

                isVehicleAlreadyInDatabase = true
                isModified = true
                break
            }

        }

        if(!isVehicleAlreadyInDatabase){
            records.add(currentVehicleData)
            isModified = true
        }

        if(isModified){
            return write(records)
        }

        return StatusCodes.NOT_MODIFIED

    }

    override fun addMultiple(vehicleReports: List<VehicleReport>) : Int{

        var isModified = false

        for(currentVehicleData in vehicleReports){

            var isVehicleAlreadyInDatabase = false

            for(element in records){

                if(element.Vehicle == currentVehicleData.Vehicle){

                    val currentTimeStamp = DataTransformer.stringToDate(element.timestampUTC)
                    val newTimeStamp = DataTransformer.stringToDate(currentVehicleData.timestampUTC)

                    if(newTimeStamp.after(currentTimeStamp)){
                        element.latitude = currentVehicleData.latitude
                        element.longitude = currentVehicleData.longitude
                        element.timestampUTC = currentVehicleData.timestampUTC
                    }

                    isVehicleAlreadyInDatabase = true
                    isModified = true
                    break
                }

            }

            if(!isVehicleAlreadyInDatabase){
                records.add(currentVehicleData)
                isModified = true
            }

        }

        if(isModified){
            return write(records)
        }

        return StatusCodes.NOT_MODIFIED

    }

    override fun rewrite(elementsToWrite: List<VehicleReport>) : Int{
        erase()
        return addMultiple(elementsToWrite)
    }

}