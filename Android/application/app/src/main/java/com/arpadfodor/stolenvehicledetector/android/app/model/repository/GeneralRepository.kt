package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import android.content.Context
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData

object GeneralRepository {

    lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    fun updateAll(callback: (isVehiclesSuccess: Boolean, isReportsSuccess: Boolean) -> Unit){
        VehicleRepository.updateFromApi{ vehiclesSuccess ->
            ReportRepository.updateFromApi{ reportsSuccess ->
                callback(vehiclesSuccess, reportsSuccess)
            }
        }
    }

    fun isFreshTimestamp(meta: MetaData, currentTimestampUTC: String) : Boolean{
        val currentDate = DateHandler.stringToDate(currentTimestampUTC)
        val dbDate = DateHandler.stringToDate(meta.modificationTimestampUTC)
        return currentDate.after(dbDate)
    }

}