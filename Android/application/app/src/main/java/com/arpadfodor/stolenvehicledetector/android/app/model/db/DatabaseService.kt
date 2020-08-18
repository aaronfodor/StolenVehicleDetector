package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.UserReport
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

    /**
     * Vehicles
     **/
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

    /**
     * Reports
     **/
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

    /**
     * User reports
     **/
    fun postUserReport(report: UserReport, callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(context)

        Thread {

            var isDbUpdated = false

            // run delete, insert, etc. in an atomic transaction
            database.runInTransaction{

                try {
                    database.userReportTable().insert(report)
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

    fun getUserReportsByUser(userId: String, callback: (List<UserReport>) -> Unit) {

        val reports = mutableListOf<UserReport>()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val dbContent = database.userReportTable().getByReporter(userId) ?: listOf()
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

    fun deleteUserReportByIdAndUser(id: Int, userId: String, callback: (Boolean) -> Unit) {

        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                database.userReportTable().deleteByIdAndReporter(id, userId)
                callback(true)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

        }.start()

    }

    fun deleteUserReportsOfUser(userId: String, callback: (Boolean) -> Unit) {

        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                database.userReportTable().deleteAllByReporter(userId)
                callback(true)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

        }.start()

    }

    /**
     * Update from API
     **/
    fun updateVehiclesFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getVehiclesMeta { size, apiTimestamp ->

            isVehiclesFreshTimestamp(apiTimestamp){ isNewer ->

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

            isReportsFreshTimestamp(apiTimestamp){ isNewer ->

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

    fun updateAll(callbackVehicles: (isVehiclesSuccess: Boolean) -> Unit,
                  callbackReports: (isReportsSuccess: Boolean) -> Unit){
        updateVehiclesFromApi(callbackVehicles)
        updateReportsFromApi(callbackReports)
    }

    private fun isVehiclesFreshTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getVehiclesMeta { meta ->
            val result = isFreshTimestamp(meta, currentTimestampUTC)
            callback(result)
        }
    }

    private fun isReportsFreshTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getReportsMeta { meta ->
            val result = isFreshTimestamp(meta, currentTimestampUTC)
            callback(result)
        }
    }

    private fun isFreshTimestamp(meta: MetaData, currentTimestampUTC: String) : Boolean{
        val currentDate = DateHandler.stringToDate(currentTimestampUTC)
        val dbDate = DateHandler.stringToDate(meta.modificationTimestampUTC)
        return currentDate.after(dbDate)
    }

}