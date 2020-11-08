package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.communication.StatusCodes
import com.arpadfodor.ktor.data.*
import com.google.gson.reflect.TypeToken
import com.arpadfodor.ktor.data.UsersRepository
import com.arpadfodor.ktor.data.dataclasses.*

class Interactor{

    companion object{

        const val SERVER_NAME = "Stolen Vehicle Detector Server"
        const val API_NAME = "API v1"

        const val META = "meta"
        const val VEHICLE = "vehicle"
        const val REPORT = "report"
        const val USER = "user"
        const val VEHICLE_HISTORY = "vehicle_history"
        const val REPORT_HISTORY = "report_history"
        const val USER_HISTORY = "user_history"

        const val PERMISSION_API_GET = "API_GET"
        const val PERMISSION_API_POST = "API_POST"
        const val PERMISSION_MODIFY_SELF = "MODIFY_SELF"
        const val PERMISSION_REGISTER = "REGISTER"
        const val PERMISSION_ADMIN = "ADMINISTRATOR"

        var metaDAO : MetaDAO = MetaDAO(META)
        var vehicleRepository : VehiclesRepository = VehiclesRepository(VEHICLE, metaDAO)
        var reportRepository : ReportRepository = ReportRepository(REPORT, metaDAO)
        var userRepository : UsersRepository = UsersRepository(USER, metaDAO)

        fun init(){
            metaDAO = MetaDAO(META)
            vehicleRepository = VehiclesRepository(VEHICLE, metaDAO)
            reportRepository = ReportRepository(REPORT, metaDAO)
            userRepository = UsersRepository(USER, metaDAO)
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
            VEHICLE -> vehicleRepository.read()
            REPORT -> reportRepository.read()
            USER -> userRepository.read()
            VEHICLE_HISTORY -> vehicleRepository.historyDAO.read()
            REPORT_HISTORY -> reportRepository.historyDAO.read()
            USER_HISTORY -> userRepository.historyDAO.read()
            else -> mutableListOf()
        }
        return DataTransformer.objectToJsonString(dbContent)
    }

    fun getMetaDataAsJson(key: String): String{
        val dbContent = when(key){
            VEHICLE -> metaDAO.getMeta(key)
            REPORT -> metaDAO.getMeta(key)
            USER -> metaDAO.getMeta(key)
            else -> MetaData("", 0, "")
        }
        return DataTransformer.objectToJsonString(dbContent)
    }

    fun addReport(reportJson: String, reporterId: String) : Int{

        val report: Report = DataTransformer.jsonToType<Report>(reportJson, object : TypeToken<Report>() {}.type)
                ?: return StatusCodes.BAD_REQUEST

        if(!isVehicleStolen(report.vehicleLicenseId)){
            return StatusCodes.BAD_REQUEST
        }

        if(!ValidatorService.isUtcTimeStampValid(report.validFromUTC)){
            return StatusCodes.BAD_REQUEST
        }

        if(!ValidatorService.isLatitudeValid(report.latitude) || !ValidatorService.isLongitudeValid(report.longitude)){
            return StatusCodes.BAD_REQUEST
        }

        val validatedReport = Report(report.vehicleLicenseId, reporterId, report.latitude, report.longitude, report.message,
                report.vehicleLicenseId, 1, report.validFromUTC, "")

        val result = reportRepository.addOrUpdate(listOf(validatedReport))
        if(result == StatusCodes.SUCCESS){
            userRepository.increaseReportCounterOfUser(reporterId)
        }
        return result
    }

    fun rawVehiclesToDatabase(vehiclesRaw: String) : Int{
        val rawVehiclesList: MutableList<RawVehicle> =
                DataTransformer.jsonToType(vehiclesRaw, object : TypeToken<MutableList<RawVehicle>>() {}.type)
                ?: return StatusCodes.BAD_REQUEST
        val vehicles = DataTransformer.transformRawData(rawVehiclesList)
        val currentTimeUTC = DataUtils.currentTimeUTC()
        vehicles.forEach { it.validFromUTC = currentTimeUTC }
        vehicleRepository.addOrUpdate(vehicles)
        return StatusCodes.SUCCESS
    }

    fun rawVehiclesFileToDatabase() : Int{
        val rawFilePath = "resources/data/raw_vehicle.json"
        val input = DataUtils.getFileContent(rawFilePath)
        return rawVehiclesToDatabase(input)
    }

    fun deleteVehicles() : Int{
        return vehicleRepository.erase()
    }

    fun deleteReports() : Int{
        return reportRepository.erase()
    }

    fun getUsers(): String{
        return DataTransformer.objectToJsonString(userRepository.read())
    }

    fun addUser(userJson: String) : Int{
        val userToAdd: User =
                DataTransformer.jsonToType<User>(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        userToAdd.validFromUTC = DataUtils.currentTimeUTC()
        return userRepository.add(listOf(userToAdd))
    }

    fun addApiUser(userJson: String) : Int{
        val userToAdd: User = DataTransformer.jsonToType<User>(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        userToAdd.validFromUTC = DataUtils.currentTimeUTC()
        userToAdd.permissions = mutableListOf(PermissionType.API_POST, PermissionType.API_GET, PermissionType.MODIFY_SELF)
        return userRepository.add(listOf(userToAdd))
    }

    fun deleteUser(key: String) : Int{
        val resultCode = userRepository.delete(key, DataUtils.currentTimeUTC())
        updateReportsBasedOnValidUsers()
        return resultCode
    }

    fun updateUser(userJson: String) : Int{
        val userToUpdate: User = DataTransformer.jsonToType<User>(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        userToUpdate.validFromUTC = DataUtils.currentTimeUTC()
        val resultCode = userRepository.updateUser(listOf(userToUpdate))
        updateReportsBasedOnValidUsers()
        return resultCode
    }

    fun login() : Int{
        return StatusCodes.SUCCESS
    }

    fun deleteSelf(key: String) : Int{
        val resultCode = userRepository.deleteSelf(key, DataUtils.currentTimeUTC())
        updateReportsBasedOnValidUsers()
        return resultCode
    }

    fun updateSelf(key: String, userJson: String) : Int{
        val userToUpdate: User = DataTransformer.jsonToType<User>(userJson, object : TypeToken<User>() {}.type) ?: return StatusCodes.BAD_REQUEST
        userToUpdate.validFromUTC = DataUtils.currentTimeUTC()
        return if(key == userToUpdate.email){
            userRepository.updateSelf(userToUpdate)
        }
        else{
            StatusCodes.UNAUTHORIZED
        }
    }

    private fun isVehicleStolen(licensePlate: String) : Boolean{
        var isVehicleStolen = false

        val vehicles = vehicleRepository.read()
        for(element in vehicles){
            if(element.licenseId == licensePlate){
                isVehicleStolen = true
                break
            }
        }

        return isVehicleStolen
    }

    private fun updateReportsBasedOnValidUsers() : Int{
        val validUserEmails = userRepository.read().filter { it.active }.map { it.email }
        return reportRepository.updateValidReporters(validUserEmails, DataUtils.currentTimeUTC())
    }

}