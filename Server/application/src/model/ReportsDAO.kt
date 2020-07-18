package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.VehicleReport
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<VehicleReport>>() {}.type

class ReportsDAO(name: String,  metaDAO: MetaDAO?) : DAO<VehicleReport>(name, typeToken, metaDAO){

    fun updateValidReporters(validReporterKeys: List<String>) : Int{
        records.retainAll { it.Reporter in validReporterKeys }
        return write(records)
    }

}