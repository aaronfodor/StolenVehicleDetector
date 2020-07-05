package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppModel{

    var stolenVehicles: StolenVehicles
    var vehicleCoordinates: VehicleCoordinates
    var vehicleReports: VehicleReports

    init {
        AuthenticationService.initialize()
        stolenVehicles = StolenVehiclesDatabase.read()
        vehicleCoordinates = VehicleCoordinatesDatabase.read()
        vehicleReports = VehicleReportsDatabase.read()
    }

    fun stolenVehiclesTimeStamp() : String{
        return stolenVehicles.meta.modificationTimeStampUTC
    }

    fun vehicleCoordinatesTimeStamp() : String{
        return vehicleCoordinates.meta.modificationTimeStampUTC
    }

    fun vehicleReportsTimeStamp() : String{
        return vehicleReports.meta.modificationTimeStampUTC
    }

    fun numStolenVehicles() : Int{
        return stolenVehicles.meta.dataSize
    }

    fun numVehicleCoordinates() : Int{
        return vehicleCoordinates.meta.dataSize
    }

    fun numVehicleReports() : Int{
        return vehicleReports.meta.dataSize
    }

    fun getStolenVehiclesAsJson(): String{
        return DataTransformer.transformObjectToString(stolenVehicles)
    }

    fun getVehicleCoordinatesAsJson(): String{
        return DataTransformer.transformObjectToString(vehicleCoordinates)
    }

    fun addReport(reportJson: String) : Boolean{

        val report: VehicleReport = Gson().fromJson(reportJson, object : TypeToken<VehicleReport>() {}.type)
                ?: return false

        if(!isVehicleStolen(report.vehicleLicensePlate)){
            return false
        }

        if(!Validator.isUtcTimeStampValid(report.detectionTimeStampUTC)){
            return false
        }

        if(!Validator.isLatitudeValid(report.latitude) || !Validator.isLongitudeValid(report.longitude)){
            return false
        }

        VehicleReportsDatabase.add(report)
        vehicleReports = VehicleReportsDatabase.read()

        if(VehicleCoordinatesDatabase.add(VehicleCoordinate(report.vehicleLicensePlate,
                report.latitude, report.longitude, report.detectionTimeStampUTC))){
            vehicleCoordinates = VehicleCoordinatesDatabase.read()
        }

        return true

    }

    private fun isVehicleStolen(licensePlate: String) : Boolean{

        var isVehicleStolen = false

        for(element in stolenVehicles.vehicles){
            if(element.name == licensePlate){
                isVehicleStolen = true
                break
            }
        }

        return isVehicleStolen

    }

    fun transformRawStolenVehiclesData(){
        val rawDataList = StolenVehiclesDatabase.readRawListFrom("raw_stolen_vehicles.json")
        val vehicles = DataTransformer.transformRawData(rawDataList)
        StolenVehiclesDatabase.write(vehicles)
        stolenVehicles = vehicles
    }

    fun validateApiUser(userName: String, password: String) : Boolean{
        val userToValidate = User(userName, password, UserType.API_USER)
        return AuthenticationService.validateCredentials(userToValidate)
    }

    fun validateAdmin(userName: String, password: String) : Boolean{
        val userToValidate = User(userName, password, UserType.ADMINISTRATOR)
        return AuthenticationService.validateCredentials(userToValidate)
    }

}