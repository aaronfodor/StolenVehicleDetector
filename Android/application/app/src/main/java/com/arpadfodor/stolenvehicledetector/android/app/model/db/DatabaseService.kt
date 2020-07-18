package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import java.util.*

object DatabaseService {

    private const val STOLEN_VEHICLES_TIMESTAMP_ID = 1L
    private const val STOLEN_VEHICLES_TIMESTAMP_NAME = "stolen vehicles"

    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    private fun setDatabase(stolenVehicles : List<StolenVehicle>, stolenVehiclesTimestamp: Date,
                            callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(context)

        Thread {

            var isDbUpdated = false

            // run delete, insert, etc. in an atomic transaction
            database.runInTransaction{

                try {
                    database.stolenVehicleTable().deleteAll()
                    database.stolenVehicleTable().insert(stolenVehicles)

                    val dateString = DateHandler.dateToString(stolenVehiclesTimestamp)
                    val timestamp = Timestamp(STOLEN_VEHICLES_TIMESTAMP_ID,
                        STOLEN_VEHICLES_TIMESTAMP_NAME, dateString)
                    database.timestampTable().deleteByName(STOLEN_VEHICLES_TIMESTAMP_NAME)
                    database.timestampTable().insert(timestamp)

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

    fun getStolenVehicleLicenses(callback: (List<String>) -> Unit) {

        val licenses = mutableListOf<String>()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val dbContent = database.stolenVehicleTable().getAll()
                for(element in dbContent){
                    licenses.add(element.licenseId)
                }
                callback(licenses)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun getByLicenseId(licenseId: String, callback: (List<StolenVehicle>) -> Unit){

        var rows : List<StolenVehicle>
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                rows = database.stolenVehicleTable().getByLicenseId(licenseId)
                callback(rows)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    private fun getStolenVehiclesTimestamp(callback: (Date) -> Unit){

        var timestamp = DateHandler.defaultDate()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val rawTimestamp = database.timestampTable().getByName(STOLEN_VEHICLES_TIMESTAMP_NAME)
                timestamp = DateHandler.stringToDate(rawTimestamp.timestampUTC)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(timestamp)
            }

        }.start()

    }

    fun updateFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getStolenVehicleTimestamp { timestamp ->

            isInputFreshTimestamp(timestamp){ isFreshTimestamp ->

                //is the API data fresh compared to app DB content?
                if(isFreshTimestamp){
                    //get content from API
                    ApiService.getStolenVehicleData { data ->

                        if(data.isNotEmpty()){
                            setDatabase(data, timestamp){ result ->
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

    private fun isInputFreshTimestamp(timestampUTC: Date, callback: (isFreshTimestamp: Boolean) -> Unit){

        getStolenVehiclesTimestamp { stolenVehiclesUpdatedAt ->
            val isAfterDbTimestamp = timestampUTC.after(stolenVehiclesUpdatedAt)
            callback(isAfterDbTimestamp)
        }

    }

}