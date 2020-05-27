package com.arpadfodor.android.stolencardetector.model.db

import android.content.Context
import androidx.room.Room
import com.arpadfodor.android.stolencardetector.model.api.ApiService

object DatabaseService {

    private const val DB_NAME = "stolen_vehicles_table"
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    private fun setDatabase(stolenVehicles : List<StolenVehicle>){

        val stolenVehicleDB = Room.databaseBuilder(context, StolenVehicleDB::class.java, DB_NAME).build()

        Thread {

            try {
                stolenVehicleDB.dbInteraction().deleteAll()
                for(element in stolenVehicles){
                    stolenVehicleDB.dbInteraction().insert(element)
                }
                //just for testing
                stolenVehicleDB.dbInteraction().insert(StolenVehicle(69, "SAMSUNG", "phone", "Samsung", "black"))
                stolenVehicleDB.dbInteraction().insert(StolenVehicle(70, "HJC759", "car", "Opel", "black"))
                stolenVehicleDB.close()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun getStolenVehicleLicenses(callback: (List<String>) -> Unit) {

        val licenses = mutableListOf<String>()
        val stolenVehicleDB = Room.databaseBuilder(context, StolenVehicleDB::class.java, DB_NAME).build()

        Thread {

            try {
                val dbContent = stolenVehicleDB.dbInteraction().getAll()
                for(element in dbContent){
                    licenses.add(element.licenseId)
                }
                stolenVehicleDB.close()
                callback(licenses)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun getByLicenseId(licenseId: String, callback: (List<StolenVehicle>) -> Unit){

        var rows : List<StolenVehicle>
        val stolenVehicleDB = Room.databaseBuilder(context, StolenVehicleDB::class.java, DB_NAME).build()

        Thread {
            try {
                rows = stolenVehicleDB.dbInteraction().getByLicenseId(licenseId)
                stolenVehicleDB.close()
                callback(rows)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

    }

    fun updateFromApi(callback: (isSuccess: Boolean) -> Unit){

        ApiService.getStolenVehicleData {

            if(it.isNotEmpty()){
                setDatabase(it)
                callback(true)
            }
            else{
                callback(false)
            }

        }

    }

}