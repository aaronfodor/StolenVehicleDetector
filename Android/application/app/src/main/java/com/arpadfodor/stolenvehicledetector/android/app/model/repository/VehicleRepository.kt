package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiVehicle
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbMetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbVehicle
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Vehicle

object VehicleRepository {

    private const val VEHICLES_META_ID = "vehicles"

    private fun setVehicles(vehicles : List<Vehicle>, vehiclesMeta: DbMetaData,
                            callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                val dbVehicleList = vehicleListToDbVehicleList(vehicles)

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {

                    database.vehicleTable().deleteAll()
                    database.vehicleTable().insert(dbVehicleList)

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

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var vehicles: List<Vehicle> = listOf()

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.vehicleTable().getAll() ?: listOf()
                    vehicles = dbVehicleListToVehicleList(dbContent)
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

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var rows : List<Vehicle> = listOf()

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.vehicleTable().getByLicenseId(licenseId) ?: listOf()
                    rows = dbVehicleListToVehicleList(dbContent)
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

    private fun getMeta(callback: (DbMetaData) -> Unit){

        Thread {

            var metaData = DbMetaData()
            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    metaData = database.metaTable().getByKey(VEHICLES_META_ID) ?: DbMetaData()
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

        ApiService.getVehiclesMeta { size, apiTimestamp ->

            isFreshTimestamp(apiTimestamp){ isNewer ->

                //is the API data fresh compared to app DB content?
                if(isNewer){
                    //get content from API
                    ApiService.getVehiclesData { apiVehicles ->

                        if(apiVehicles.isNotEmpty()){

                            val vehiclesMeta = DbMetaData(VEHICLES_META_ID, size, apiTimestamp)
                            val transformedVehicles = apiVehicleListToVehicleList(apiVehicles)

                            setVehicles(transformedVehicles, vehiclesMeta){ result ->
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

    private fun vehicleToDbVehicle(source: Vehicle) : DbVehicle {
        return DbVehicle(source.licenseId, source.type, source.manufacturer, source.color)
    }

    private fun dbVehicleToVehicle(source: DbVehicle) : Vehicle {
        return Vehicle(source.licenseId, source.type, source.manufacturer, source.color)
    }

    private fun apiVehicleToVehicle(source: ApiVehicle) : Vehicle {
        return Vehicle(source.name, source.type, source.manufacturer, source.color)
    }

    private fun vehicleListToDbVehicleList(sourceList: List<Vehicle>) : List<DbVehicle>{
        val dbVehicleList = mutableListOf<DbVehicle>()
        for(element in sourceList){
            val constructed = vehicleToDbVehicle(element)
            dbVehicleList.add(constructed)
        }
        return dbVehicleList
    }

    private fun dbVehicleListToVehicleList(sourceList: List<DbVehicle>) : List<Vehicle>{
        val vehicleList = mutableListOf<Vehicle>()
        for(element in sourceList){
            val constructed = dbVehicleToVehicle(element)
            vehicleList.add(constructed)
        }
        return vehicleList
    }

    private fun apiVehicleListToVehicleList(sourceList: List<ApiVehicle>) : List<Vehicle>{
        val vehicleList = mutableListOf<Vehicle>()
        for(element in sourceList){
            val constructed = apiVehicleToVehicle(element)
            vehicleList.add(constructed)
        }
        return vehicleList
    }

}