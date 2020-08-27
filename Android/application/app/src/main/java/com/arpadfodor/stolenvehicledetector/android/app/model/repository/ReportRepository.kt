package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiReport
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbMetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbReport
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Report

object ReportRepository {

    private const val REPORTS_META_ID = "reports"

    private fun setReports(reportList : List<Report>, reportsMeta: DbMetaData,
                           callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                val dbReportList = reportListToDbReportList(reportList)

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {

                    database.reportTable().deleteAll()
                    database.reportTable().insert(dbReportList)

                    database.metaTable().deleteByKey(REPORTS_META_ID)
                    database.metaTable().insert(reportsMeta)

                }
                //update the local flag
                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    /**
     * Reports
     **/
    fun getReports(callback: (List<Report>) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var reports: List<Report> = listOf()

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.reportTable().getAll() ?: listOf()
                    reports = dbReportListToReportList(dbContent)
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(reports)
            }

        }.start()

    }

    private fun getMeta(callback: (DbMetaData) -> Unit){

        Thread {

            var metaData = DbMetaData()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    metaData = database.metaTable().getByKey(REPORTS_META_ID) ?: DbMetaData()
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(metaData)
            }

        }.start()

    }

    fun updateFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getReportsMeta { size, apiTimestamp ->

            isFreshTimestamp(apiTimestamp){ isNewer ->

                //is the API data fresh compared to app DB content?
                if(isNewer){
                    //get content from API
                    ApiService.getReportsData { apiReportList ->

                        if(apiReportList.isNotEmpty()){

                            val reportsMeta = DbMetaData(REPORTS_META_ID, size, apiTimestamp)
                            val transformedReports = apiReportListToReportList(apiReportList)

                            setReports(transformedReports, reportsMeta){ result ->
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

    private fun isFreshTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getMeta { meta ->
            val result = GeneralRepository.isFreshTimestamp(meta, currentTimestampUTC)
            callback(result)
        }
    }

    private fun apiReportToReport(source: ApiReport) : Report{
        return Report(source.Id, source.Vehicle, source.Reporter,
            source.latitude, source.longitude, source.message, source.timestampUTC)
    }

    private fun reportToDbReport(source: Report) : DbReport{
        return DbReport(source.Id, source.Vehicle, source.Reporter,
            source.latitude, source.longitude, source.message, source.timestampUTC)
    }

    private fun dbReportToReport(source: DbReport) : Report{
        return Report(source.Id, source.Vehicle, source.Reporter,
            source.latitude, source.longitude, source.message, source.timestampUTC)
    }

    private fun apiReportListToReportList(sourceList: List<ApiReport>) : List<Report>{
        val reportList = mutableListOf<Report>()
        for(element in sourceList){
            val constructed = apiReportToReport(element)
            reportList.add(constructed)
        }
        return reportList
    }

    private fun reportListToDbReportList(sourceList: List<Report>) : List<DbReport>{
        val dbReportList = mutableListOf<DbReport>()
        for(element in sourceList){
            val constructed = reportToDbReport(element)
            dbReportList.add(constructed)
        }
        return dbReportList
    }

    private fun dbReportListToReportList(sourceList: List<DbReport>) : List<Report>{
        val reportList = mutableListOf<Report>()
        for(element in sourceList){
            val constructed = dbReportToReport(element)
            reportList.add(constructed)
        }
        return reportList
    }

}