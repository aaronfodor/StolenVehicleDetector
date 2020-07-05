package com.arpadfodor.stolenvehicledetector.android.app.model.api

import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.arpadfodor.stolenvehicledetector.android.app.model.db.StolenVehicle
import java.text.SimpleDateFormat
import java.util.*

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
                val dataCall = stolenVehicleAPI.getData()
                val dataResponse = dataCall.execute().body() ?: StolenVehiclesJson(emptyList())
                val transformedDataResponse = dataResponseTransform(dataResponse)
                callback(transformedDataResponse)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }.start()

    }

    fun getStolenVehicleTimestamp(callback: (Date) -> Unit) {

        Thread {
            //for testing, should be DateHandler.defaultDate()
            var timestampUTC = DateHandler.stringToDate("1980-01-01 01:01:01")
            try {
                val timestampCall = stolenVehicleAPI.getTimestamp()
                val timestampResponse = timestampCall.execute().body()
                    ?: TimestampJson(DateHandler.dateToString(timestampUTC))
                timestampUTC = DateHandler.stringToDate(timestampResponse.timestampUTC)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            callback(timestampUTC)
        }.start()

    }

    private fun dataResponseTransform(content: StolenVehiclesJson) : List<StolenVehicle>{

        val stolenVehiclesList = mutableListOf<StolenVehicle>()

        var cntr = 1

        for(i in content.vehicle.indices){

            val current = content.vehicle[i]

            val constructed = StolenVehicle(
                cntr.toLong(),
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
                cntr++
            }
        }

        //just for testing
        cntr++
        stolenVehiclesList.add(StolenVehicle(cntr.toLong(), "SAMSUNG", "phone", "Samsung", "black"))
        cntr++
        stolenVehiclesList.add(StolenVehicle(cntr.toLong(), "HJC759", "car", "Opel", "black"))

        return stolenVehiclesList

    }

}