package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VehicleReportsDatabase{

    val dbPath = "resources/"
    val dbName = "vehicle_reports.json"

    fun read() : VehicleReports{
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val reports: VehicleReports = Gson().fromJson(content, object : TypeToken<VehicleReports>() {}.type)
                ?: VehicleReports(mutableListOf(), MetaData(0, "0"))
        return reports
    }

    fun add(vehicleReport: VehicleReport){
        val content = read()
        content.reports.add(vehicleReport)
        content.meta.modificationTimeStampUTC = DataUtils.currentTimeUTC()
        content.meta.dataSize = content.reports.size
        val dbContent = DataTransformer.objectToJsonString(content)
        File("${dbPath}${dbName}").writeText(dbContent)
    }

}