package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.Report
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<Report>>() {}.type

class ReportHistoryDAO(name: String, metaDAO: MetaDAO?) : DAO<Report>(name, typeToken, metaDAO){

    fun updateValidReporters(validReporterKeys: List<String>) : Int{
        records.retainAll { it.Reporter in validReporterKeys }
        return write(records)
    }

}