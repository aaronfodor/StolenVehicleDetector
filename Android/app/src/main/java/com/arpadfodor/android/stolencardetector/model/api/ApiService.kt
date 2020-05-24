package com.arpadfodor.android.stolencardetector.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.arpadfodor.android.stolencardetector.model.db.StolenVehicle

object ApiService{

    lateinit var stolenVehicleAPI: StolenVehicleAPI

    fun initialize(){

        val retrofitStolenCarsAPI = Retrofit.Builder()
            .baseUrl(StolenVehicleAPI.ENDPOINT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.stolenVehicleAPI = retrofitStolenCarsAPI.create(StolenVehicleAPI::class.java)

    }

    fun getStolenVehicleData(callback: (List<StolenVehicle>) -> Unit) {

        Thread {

            try {
                val call = stolenVehicleAPI.getData()
                val response = call.execute().body() ?: StolenVehiclesJson(emptyList())
                val transformedResponse = apiResponseTransform(response)
                callback(transformedResponse)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }

        }.start()

    }

    private fun apiResponseTransform(content: StolenVehiclesJson) : List<StolenVehicle>{

        val stolenVehiclesList = mutableListOf<StolenVehicle>()

        for(i in content.stolenVehicles.indices){

            val current = content.stolenVehicles[i]
            val constructed = StolenVehicle(
                i.toLong(),
                current.licenseId.replace("rendszám", "", true)
                    .replace(":", "").replace(" ", ""),
                current.vehicleType.replace("jármű fajta", "", true)
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

        return stolenVehiclesList

    }

}