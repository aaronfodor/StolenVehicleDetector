package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppModel{

    init {
        CredentialsDatabase.initialize()
        StolenVehiclesDatabase.initialize()
        VehicleCoordinatesDatabase.initialize()
        VehicleReportsDatabase.initialize()
    }

    fun stolenVehiclesTimeStamp() : String{
        return StolenVehiclesDatabase.stolenVehicles.meta.modificationTimeStampUTC
    }

    fun vehicleCoordinatesTimeStamp() : String{
        return VehicleCoordinatesDatabase.vehicleCoordinates.meta.modificationTimeStampUTC
    }

    fun vehicleReportsTimeStamp() : String{
        return VehicleReportsDatabase.vehicleReports.meta.modificationTimeStampUTC
    }

    fun numStolenVehicles() : Int{
        return StolenVehiclesDatabase.stolenVehicles.meta.dataSize
    }

    fun numVehicleCoordinates() : Int{
        return VehicleCoordinatesDatabase.vehicleCoordinates.meta.dataSize
    }

    fun numVehicleReports() : Int{
        return VehicleReportsDatabase.vehicleReports.meta.dataSize
    }

    fun getStolenVehiclesAsJson(): String{
        return DataTransformer.objectToJsonString(StolenVehiclesDatabase.stolenVehicles)
    }

    fun getStolenVehiclesMetaAsJson(): String{
        return DataTransformer.objectToJsonString(StolenVehiclesDatabase.stolenVehicles.meta)
    }

    fun getVehicleCoordinatesAsJson(): String{
        return DataTransformer.objectToJsonString(VehicleCoordinatesDatabase.vehicleCoordinates)
    }

    fun getVehicleCoordinatesMetaAsJson(): String{
        return DataTransformer.objectToJsonString(VehicleCoordinatesDatabase.vehicleCoordinates.meta)
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
        VehicleReportsDatabase.vehicleReports = VehicleReportsDatabase.read()

        if(VehicleCoordinatesDatabase.add(VehicleCoordinate(report.vehicleLicensePlate,
                report.latitude, report.longitude, report.detectionTimeStampUTC))){
            VehicleCoordinatesDatabase.vehicleCoordinates = VehicleCoordinatesDatabase.read()
        }

        return true

    }

    fun rawStolenVehiclesToDatabase(stolenVehiclesRaw: String) : Boolean{
        val stolenVehiclesList: MutableList<StolenVehicle> = Gson().fromJson(stolenVehiclesRaw,
                object : TypeToken<MutableList<StolenVehicle>>() {}.type) ?: return false
        StolenVehiclesDatabase.rewrite(stolenVehiclesList)
        return true
    }

    fun rawStolenVehiclesFileToDatabase(){
        val rawFilePath = "raw_stolen_vehicles.json"
        val input = DataUtils.getFileContent("${StolenVehiclesDatabase.dbPath}${rawFilePath}")
        rawStolenVehiclesToDatabase(input)
    }

    fun deleteVehicles() : Boolean{
        StolenVehiclesDatabase.erase()
        return StolenVehiclesDatabase.stolenVehicles.vehicles.isEmpty()
    }

    fun deleteCoordinates() : Boolean{
        VehicleCoordinatesDatabase.erase()
        return VehicleCoordinatesDatabase.vehicleCoordinates.coordinates.isEmpty()
    }

    fun deleteReports() : Boolean{
        VehicleReportsDatabase.erase()
        return VehicleReportsDatabase.vehicleReports.reports.isEmpty()
    }

    fun getUsers(): String{
        return DataTransformer.objectToJsonString(CredentialsDatabase.getUsers())
    }

    fun addUser(userJson: String) : Boolean{

        val userToAdd: User = Gson().fromJson(userJson, object : TypeToken<User>() {}.type) ?: return false

        if(userToAdd.name == "" || userToAdd.password == "" || userToAdd.types.isEmpty()){
            return false
        }

        return CredentialsDatabase.addUser(userToAdd)

    }

    fun deleteUser(userNameToDelete: String) : Boolean{
        return CredentialsDatabase.deleteUser(userNameToDelete)
    }

    private fun isVehicleStolen(licensePlate: String) : Boolean{

        var isVehicleStolen = false

        for(element in StolenVehiclesDatabase.stolenVehicles.vehicles){
            if(element.name == licensePlate){
                isVehicleStolen = true
                break
            }
        }

        return isVehicleStolen

    }

    fun validateApiUser(userName: String, password: String) : Boolean{
        val userToValidate = User(userName, password, mutableListOf(UserType.API_USER))
        return CredentialsDatabase.validateCredentials(userToValidate)
    }

    fun validateAdmin(userName: String, password: String) : Boolean{
        val userToValidate = User(userName, password, mutableListOf(UserType.ADMINISTRATOR))
        return CredentialsDatabase.validateCredentials(userToValidate)
    }

}