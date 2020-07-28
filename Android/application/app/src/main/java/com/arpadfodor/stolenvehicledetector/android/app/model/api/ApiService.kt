package com.arpadfodor.stolenvehicledetector.android.app.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle

object ApiService{

    lateinit var stolenVehicleAPI: StolenVehicleAPI

    fun initialize(){

        val retrofitStolenCarsAPI = Retrofit.Builder()
            .baseUrl(StolenVehicleAPI.ENDPOINT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.stolenVehicleAPI = retrofitStolenCarsAPI.create(StolenVehicleAPI::class.java)

    }

    fun getStolenVehicleData(callback: (List<Vehicle>) -> Unit) {

        Thread {
            try {
                val dataCall = stolenVehicleAPI.getData()
                val dataResponse = dataCall.execute().body() ?: emptyList<ApiVehicle>()
                val transformedDataResponse = dataResponseTransform(dataResponse)
                callback(transformedDataResponse)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }.start()

    }

    fun getStolenVehiclesMeta(callback: (Int, String) -> Unit) {

        Thread {
            var size = 0
            //TODO: for testing, should be DateHandler.defaultDate()
            var timestampUTC = "1980-01-01 01:01:01"
            try {
                val metaDataCall = stolenVehicleAPI.getMetaData()
                val metaDataResponse = metaDataCall.execute().body()
                    ?: ApiMetaData(0, timestampUTC)
                size = metaDataResponse.dataSize
                timestampUTC = metaDataResponse.modificationTimestampUTC
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            callback(size, timestampUTC)
        }.start()

    }

    //TODO: post report implementation
    fun postReport(report: ApiVehicleReport, callback: (Boolean) -> Unit){

        Thread {

            try {
                val a = 2
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
            callback(true)

        }.start()

    }

    //TODO: report, user, self interactions

    private fun dataResponseTransform(content: List<ApiVehicle>) : List<Vehicle>{

        val stolenVehiclesList = mutableListOf<Vehicle>()

        for(i in content.indices){

            val current = content[i]

            val constructed =
                Vehicle(
                    current.name.replace("rendszám", "", true)
                        .replace(":", "").replace(" ", ""),
                    current.type.replace("jármű fajta", "", true)
                        .replace(":", "").replace(" ", ""),
                    current.manufacturer.replace("gyártmány", "", true)
                        .replace(":", "").replace(" ", ""),
                    current.color.replace("szín", "", true)
                        .replace(":", "").replace(" ", "")
                )

            if(constructed.licenseId.isNotBlank()){
                stolenVehiclesList.add(constructed)
            }
        }

        //just for testing
        stolenVehiclesList.add(
            Vehicle(
                "SAMSUNG",
                "phone",
                "Samsung",
                "black"
            )
        )
        stolenVehiclesList.add(
            Vehicle(
                "HJC759",
                "car",
                "Opel",
                "black"
            )
        )

        return stolenVehiclesList

    }

}