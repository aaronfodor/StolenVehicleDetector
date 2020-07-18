package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.StatusCodes
import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import model.dataclasses.RawVehicle

class AppModel{

    companion object{

        const val SERVER_NAME = "Stolen Vehicle Detector Server"
        const val API_NAME = "API v1"

        const val META = "meta"
        const val VEHICLES = "vehicles"
        const val REPORTS = "reports"
        const val CURRENTS = "currents"
        const val USERS = "users"

        const val PERMISSION_API_GET = "API_GET"
        const val PERMISSION_API_POST = "API_POST"
        const val PERMISSION_MODIFY_SELF = "MODIFY_SELF"
        const val PERMISSION_REGISTER = "REGISTER"
        const val PERMISSION_ADMIN = "ADMINISTRATOR"

        var metaDAO : MetaDAO = MetaDAO(META)
        var vehiclesDAO : VehiclesDAO = VehiclesDAO(VEHICLES, metaDAO)
        var reportsDAO : ReportsDAO = ReportsDAO(REPORTS, metaDAO)
        var currentsDAO : CurrentsDAO = CurrentsDAO(CURRENTS, metaDAO)
        var usersDAO : UsersDAO = UsersDAO(USERS, metaDAO)

        fun init(){
            metaDAO = MetaDAO(META)
            vehiclesDAO = VehiclesDAO(VEHICLES, metaDAO)
            reportsDAO = ReportsDAO(REPORTS, metaDAO)
            currentsDAO = CurrentsDAO(CURRENTS, metaDAO)
            usersDAO = UsersDAO(USERS, metaDAO)
        }

    }

    fun getTableSize(key: String) : Int{
        return metaDAO.getSize(key)
    }

    fun getTableModificationTimestamp(key: String) : String{
        return metaDAO.getModificationTimestamp(key)
    }

    fun getDataAsJson(key: String): String{
        val dbContent = when(key){
            META -> metaDAO.read()
            VEHICLES -> vehiclesDAO.read()
            REPORTS -> reportsDAO.read()
            CURRENTS -> currentsDAO.read()
            USERS -> usersDAO.read()
            else -> mutableListOf()
        }
        return DataTransformer.objectToJsonString(dbContent)
    }

    fun getMetaDataAsJson(key: String): String{
        val dbContent = when(key){
            VEHICLES -> metaDAO.getMeta(key)
            REPORTS -> metaDAO.getMeta(key)
            CURRENTS -> metaDAO.getMeta(key)
            USERS -> metaDAO.getMeta(key)
            else -> MetaData("", 0, "")
        }
        return DataTransformer.objectToJsonString(dbContent)
    }

    fun addReport(reportJson: String, reporterId: String) : Int{

        val report: VehicleReport = Gson().fromJson(reportJson, object : TypeToken<VehicleReport>() {}.type)
                ?: return StatusCodes.BAD_REQUEST

        if(!isVehicleStolen(report.Vehicle)){
            return StatusCodes.BAD_REQUEST
        }

        if(!Validator.isUtcTimeStampValid(report.timestampUTC)){
            return StatusCodes.BAD_REQUEST
        }

        if(!Validator.isLatitudeValid(report.latitude) || !Validator.isLongitudeValid(report.longitude)){
            return StatusCodes.BAD_REQUEST
        }

        val validatedReport = VehicleReport(1, report.Vehicle, reporterId,
                report.latitude, report.longitude, report.message, report.timestampUTC)

        reportsDAO.add(validatedReport)
        return currentsDAO.add(validatedReport)

    }

    fun rawStolenVehiclesToDatabase(stolenVehiclesRaw: String) : Int{
        val rawStolenVehiclesList: MutableList<RawVehicle> = Gson().fromJson(stolenVehiclesRaw,
                object : TypeToken<MutableList<RawVehicle>>() {}.type) ?: return StatusCodes.BAD_REQUEST
        val stolenVehicles = DataTransformer.transformRawData(rawStolenVehiclesList)
        vehiclesDAO.rewrite(stolenVehicles)
        return StatusCodes.SUCCESS
    }

    fun rawStolenVehiclesFileToDatabase() : Int{
        val rawFilePath = "resources/data/raw_stolen_vehicles.json"
        val input = DataUtils.getFileContent(rawFilePath)
        return rawStolenVehiclesToDatabase(input)
    }

    fun deleteVehicles() : Int{
        return vehiclesDAO.erase()
    }

    fun deleteCoordinates() : Int{
        return currentsDAO.erase()
    }

    fun deleteReports() : Int{
        return reportsDAO.erase()
    }

    fun getUsers(): String{
        return DataTransformer.objectToJsonString(usersDAO.read())
    }

    fun addUser(userJson: String) : Int{
        val userToAdd: User = Gson().fromJson(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        return usersDAO.add(userToAdd)
    }

    fun addApiUser(userJson: String) : Int{
        val userToAdd: User = Gson().fromJson(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        userToAdd.permissions = mutableListOf(PermissionType.API_POST, PermissionType.API_GET, PermissionType.MODIFY_SELF)
        return usersDAO.add(userToAdd)
    }

    fun deleteUser(key: String) : Int{
        val resultCode = usersDAO.delete(key)
        updateReportsAndCurrentsBasedOnUsers()
        return resultCode
    }

    fun updateUser(userJson: String) : Int{
        val userToUpdate: User = Gson().fromJson(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        val resultCode = usersDAO.updateUser(userToUpdate)
        updateReportsAndCurrentsBasedOnUsers()
        return resultCode
    }

    fun deleteSelf(key: String) : Int{
        val resultCode = usersDAO.deleteSelf(key)
        updateReportsAndCurrentsBasedOnUsers()
        return resultCode
    }

    fun updateSelf(key: String, userJson: String) : Int{
        val userToUpdate: User = Gson().fromJson(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        return usersDAO.updateSelf(key, userToUpdate)
    }

    private fun isVehicleStolen(licensePlate: String) : Boolean{

        var isVehicleStolen = false

        val vehicles = vehiclesDAO.read()
        for(element in vehicles){
            if(element.licenseId == licensePlate){
                isVehicleStolen = true
                break
            }
        }

        return isVehicleStolen

    }

    fun validateUser(email: String, password: String, permission: String) : Int{
        val permissionToValidate = when(permission){
            PERMISSION_API_GET -> PermissionType.API_GET
            PERMISSION_API_POST -> PermissionType.API_POST
            PERMISSION_MODIFY_SELF -> PermissionType.MODIFY_SELF
            PERMISSION_REGISTER -> PermissionType.REGISTER
            PERMISSION_ADMIN -> PermissionType.ADMINISTRATOR
            else -> return StatusCodes.BAD_REQUEST
        }
        val userToValidate = User(email, "", password, "", true, mutableListOf(permissionToValidate))
        return usersDAO.validateCredentials(userToValidate)
    }

    fun updateReportsAndCurrentsBasedOnUsers() : Int{
        val validUserKeys = usersDAO.read().filter { it.active }.map { it.email }
        reportsDAO.updateValidReporters(validUserKeys)
        val validReports = reportsDAO.read()
        return currentsDAO.addMultiple(validReports)
    }

}