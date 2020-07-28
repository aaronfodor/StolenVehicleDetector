package com.arpadfodor.stolenvehicledetector.android.app.model.db

import android.content.Context
import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

object DatabaseService {

    private const val VEHICLES_META_ID = "vehicles"

    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    private fun setDatabase(vehicles : List<Vehicle>, vehiclesMeta: MetaData,
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

    fun getStolenVehicleLicenses(callback: (List<String>) -> Unit) {

        val licenses = mutableListOf<String>()
        val database = ApplicationDB.getDatabase(context)

        Thread {

            try {
                val dbContent = database.vehicleTable().getAll() ?: listOf()
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

    fun getByLicenseId(licenseId: String, callback: (List<Vehicle>) -> Unit){

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

    fun updateFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getStolenVehiclesMeta { size, apiTimestamp ->

            isInputNewerTimestamp(apiTimestamp){ isNewer ->

                //is the API data fresh compared to app DB content?
                if(isNewer){
                    //get content from API
                    ApiService.getStolenVehicleData { vehicles ->

                        if(vehicles.isNotEmpty()){
                            val vehiclesMeta = MetaData(VEHICLES_META_ID, size, apiTimestamp)
                            setDatabase(vehicles, vehiclesMeta){ result ->
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

    private fun isInputNewerTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getVehiclesMeta { meta ->
            val currentDate = DateHandler.stringToDate(currentTimestampUTC)
            val dbDate = DateHandler.stringToDate(meta.modificationTimestampUTC)
            val isAfterDbTimestamp = currentDate.after(dbDate)
            callback(isAfterDbTimestamp)
        }
    }

}