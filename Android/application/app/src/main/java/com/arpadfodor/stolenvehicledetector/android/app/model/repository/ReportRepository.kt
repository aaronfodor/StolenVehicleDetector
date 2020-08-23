package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Report

object ReportRepository {

    private const val REPORTS_META_ID = "reports"

    private fun setReports(reports : List<Report>, reportsMeta: MetaData,
                           callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {

                    database.reportTable().deleteAll()
                    database.reportTable().insert(reports)

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

            val reports = mutableListOf<Report>()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.reportTable().getAll() ?: listOf()
                    for (element in dbContent) {
                        reports.add(element)
                    }
                }
                callback(reports)

            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    private fun getMeta(callback: (MetaData) -> Unit){

        Thread {

            var metaData = MetaData()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    metaData = database.metaTable().getByKey(REPORTS_META_ID) ?: MetaData()
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

    private fun isFreshTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getMeta { meta ->
            val result = GeneralRepository.isFreshTimestamp(meta, currentTimestampUTC)
            callback(result)
        }
    }

}