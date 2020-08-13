package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Report
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

object DatabaseService {

    private const val VEHICLES_META_ID = "vehicles"
    private const val REPORTS_META_ID = "reports"

    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    private fun setVehicles(vehicles : List<Vehicle>, vehiclesMeta: MetaData,
                            callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(context)

        Thread {

            var isDbUpdated = false

            // run delete, insert, etc. in an atomic transaction
            database.runInTransaction{

                try {
                    database.vehicleTable().deleteAll()
                    database.vehicleTable().insert(vehicles)

                    database.metaTable().deleteByKey(VEHICLES_META_ID)
                    database.metaTable().insert(vehiclesMeta)

                    //update the local flag
                    isDbUpdated = true
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
                finally {
                    callback(isDbUpdated)
                }

            }

        }.start()

    }

    private fun setReports(reports : List<Report>, reportsMeta: MetaData,
                            callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(context)

        Thread {

            var isDbUpdated = false

            // run delete, insert, etc. in an atomic transaction
            database.runInTransaction{

                try {
                    database.reportTable().deleteAll()
                    database.reportTable().insert(reports)

                    database.metaTable().deleteByKey(REPORTS_META_ID)
                    database.metaTable().insert(reportsMeta)

                    //update the local flag
                    isDbUpdated = true
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
                finally {
                    callback(isDbUpdated)
                }

            }

        }.start()

    }

    fun getVehicles(callback: (List<Vehicle>) -> Unit) {

        val vehicles = mutableListOf<Vehicle>()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val dbContent = database.vehicleTable().getAll() ?: listOf()
                for(element in dbContent){
                    vehicles.add(element)
                }
                callback(vehicles)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun getVehicleByLicenseId(licenseId: String, callback: (List<Vehicle>) -> Unit){

        var rows : List<Vehicle>
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                rows = database.vehicleTable().getByLicenseId(licenseId) ?: listOf()
                callback(rows)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun getReports(callback: (List<Report>) -> Unit) {

        val reports = mutableListOf<Report>()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val dbContent = database.reportTable().getAll() ?: listOf()
                for(element in dbContent){
                    reports.add(element)
                }
                callback(reports)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    private fun getVehiclesMeta(callback: (MetaData) -> Unit){

        var metaData = MetaData()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                metaData = database.metaTable().getByKey(VEHICLES_META_ID) ?: MetaData()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(metaData)
            }

        }.start()

    }

    private fun getReportsMeta(callback: (MetaData) -> Unit){

        var metaData = MetaData()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                metaData = database.metaTable().getByKey(REPORTS_META_ID) ?: MetaData()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(metaData)
            }

        }.start()

    }

    fun updateVehiclesFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getVehiclesMeta { size, apiTimestamp ->

            isInputNewerTimestamp(apiTimestamp){ isNewer ->

                //is the API data fresh compared to app DB content?
                if(isNewer){
                    //get content from API
                    ApiService.getVehiclesData { vehicles ->

                        if(vehicles.isNotEmpty()){
                            val vehiclesMeta = MetaData(VEHICLES_META_ID, size, apiTimestamp)
                            setVehicles(vehicles, vehiclesMeta){ result ->
                                callback(result)
                            }
                        }
                        //empty content means fetching data was unsuccessful
                        else{
                            callback(false)
                        }

                    }

                }
                //when app DB is up to date, no need to fetch data from API
                else{
                    callback(true)
                }

            }

        }

    }

    fun updateReportsFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getReportsMeta { size, apiTimestamp ->

            isInputNewerTimestamp(apiTimestamp){ isNewer ->

                //is the API data fresh compared to app DB content?
                if(isNewer){
                    //get content from API
                    ApiService.getReportsData { reports ->

                        if(reports.isNotEmpty()){
                            val reportsMeta = MetaData(REPORTS_META_ID, size, apiTimestamp)
                            setReports(reports, reportsMeta){ result ->
                                callback(result)
                            }
                        }
                        //empty content means fetching data was unsuccessful
                        else{
                            callback(false)
                        }

                    }

                }
                //when app DB is up to date, no need to fetch data from API
                else{
                    callback(true)
                }

            }

        }

    }

    fun updateAllFromApi(callbackVehicles: (isVehiclesSuccess: Boolean) -> Unit,
        callbackReports: (isReportsSuccess: Boolean) -> Unit){
        updateVehiclesFromApi(callbackVehicles)
        updateReportsFromApi(callbackReports)
    }

    private fun isInputNewerTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getVehiclesMeta { meta ->
            val currentDate = DateHandler.stringToDate(currentTimestampUTC)
            val dbDate = DateHandler.stringToDate(meta.modificationTimestampUTC)
            val isAfterDbTimestamp = currentDate.after(dbDate)
            callback(isAfterDbTimestamp)
        }
    }

}