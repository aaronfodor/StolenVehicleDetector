package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

object VehicleRepository {

    private const val VEHICLES_META_ID = "vehicles"

    private fun setVehicles(vehicles : List<Vehicle>, vehiclesMeta: MetaData,
                            callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

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

    /**
     * Vehicles
     **/
    fun getVehicles(callback: (List<Vehicle>) -> Unit) {

        val vehicles = mutableListOf<Vehicle>()
        val database = ApplicationDB.getDatabase(GeneralRepository.context)

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

    fun getByLicenseId(licenseId: String, callback: (List<Vehicle>) -> Unit){

        var rows : List<Vehicle>
        val database = ApplicationDB.getDatabase(GeneralRepository.context)

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

    private fun getMeta(callback: (MetaData) -> Unit){

        var metaData = MetaData()
        val database = ApplicationDB.getDatabase(GeneralRepository.context)

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
     * Update from API
     **/
    fun updateFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getVehiclesMeta { size, apiTimestamp ->

            isFreshTimestamp(apiTimestamp){ isNewer ->

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

    private fun isFreshTimestamp(currentTimestampUTC: String, callback: (isFreshTimestamp: Boolean) -> Unit){
        getMeta { meta ->
            val result = GeneralRepository.isFreshTimestamp(meta, currentTimestampUTC)
            callback(result)
        }
    }

}