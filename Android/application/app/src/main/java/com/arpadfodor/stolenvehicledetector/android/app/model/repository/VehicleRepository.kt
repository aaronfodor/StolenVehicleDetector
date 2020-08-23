package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.MetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

object VehicleRepository {

    private const val VEHICLES_META_ID = "vehicles"

    private fun setVehicles(vehicles : List<Vehicle>, vehiclesMeta: MetaData,
                            callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {

                    database.vehicleTable().deleteAll()
                    database.vehicleTable().insert(vehicles)

                    database.metaTable().deleteByKey(VEHICLES_META_ID)
                    database.metaTable().insert(vehiclesMeta)

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
     * Vehicles
     **/
    fun getVehicles(callback: (List<Vehicle>) -> Unit) {

        Thread {

            val vehicles = mutableListOf<Vehicle>()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.vehicleTable().getAll() ?: listOf()
                    for (element in dbContent) {
                        vehicles.add(element)
                    }
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(vehicles)
            }

        }.start()

    }

    fun getByLicenseId(licenseId: String, callback: (List<Vehicle>) -> Unit){

        Thread {

            var rows : List<Vehicle> = listOf()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    rows = database.vehicleTable().getByLicenseId(licenseId) ?: listOf()
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(rows)
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
                    metaData = database.metaTable().getByKey(VEHICLES_META_ID) ?: MetaData()
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