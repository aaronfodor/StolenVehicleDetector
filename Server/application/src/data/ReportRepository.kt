package com.arpadfodor.stolenvehicledetector.data

import com.google.gson.reflect.TypeToken
import com.arpadfodor.stolenvehicledetector.data.dataclasses.Report
import com.arpadfodor.stolenvehicledetector.model.DataTransformer

private val typeToken = object : TypeToken<MutableList<Report>>() {}.type

class ReportRepository(name: String, metaDAO: MetaDAO) : DataclassDAO<Report>(name, typeToken, metaDAO){

    fun addOrUpdate(items: List<Report>): Int{

        val areSameItems: (Report, Report) -> Boolean = {record, item ->
            (record.vehicleLicenseId == item.vehicleLicenseId)
        }

        val isUpdateNeeded: (Report, Report) -> Boolean = {record, item ->
            val itemValidFromUTC = DataTransformer.stringToDate(item.validFromUTC)
            val recordValidFromUTC = DataTransformer.stringToDate(record.validFromUTC)
            val result = (itemValidFromUTC.after(recordValidFromUTC))
            result
        }

        val updateRecordBasedOnItem: (Report, Report) -> Unit = {record, item ->
            record.latitude = item.latitude
            record.longitude = item.longitude
            record.message = item.message
            record.reporterEmail = item.reporterEmail
        }

        return super.addOrUpdate(items, areSameItems = areSameItems, isUpdateNeeded = isUpdateNeeded,
                updateRecordBasedOnItem = updateRecordBasedOnItem)
    }

    fun updateValidReporters(validReporterEmails: List<String>, currentTimeUTC: String) : Int{
        records.retainAll { it.reporterEmail in validReporterEmails }
        val result = super.write(records)

        val historyContent = historyDAO.read()
        historyContent.forEach { historyItem ->

            if(!records.any { it.reporterEmail == historyItem.reporterEmail }){

                if(historyItem.validToUTC == ""){
                    historyItem.validToUTC = currentTimeUTC
                }

            }

        }
        historyDAO.write(historyContent)

        return result
    }

}