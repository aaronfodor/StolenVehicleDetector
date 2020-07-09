package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VehicleReportsDatabase{

    val dbPath = "resources/"
    val dbName = "vehicle_reports.json"

    var vehicleReports = read()

    fun initialize(){
        vehicleReports = read()
    }

    fun read() : VehicleReports{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val reports: VehicleReports = Gson().fromJson(content, object : TypeToken<VehicleReports>() {}.type)
                ?: VehicleReports(mutableListOf(), MetaData(0, "0"))
        return reports
    }

    fun add(vehicleReport: VehicleReport){
        vehicleReports = read()
        vehicleReports.reports.add(vehicleReport)
        vehicleReports.meta = updateMeta(vehicleReports.reports)
        write(vehicleReports)
    }

    fun erase(){
        vehicleReports = read()
        vehicleReports.reports.clear()
        vehicleReports.meta = updateMeta(vehicleReports.reports)
        write(vehicleReports)
    }

    private fun write(vehicleReports: VehicleReports){
        val dbContent = DataTransformer.objectToJsonString(vehicleReports)
        File("${dbPath}${dbName}").writeText(dbContent)
    }

    private fun updateMeta(vehicleReports: MutableList<VehicleReport>) : MetaData{
        val dataSize = vehicleReports.size
        val modificationTimeStampUTC = DataUtils.currentTimeUTC()
        return MetaData(dataSize, modificationTimeStampUTC)
    }

}