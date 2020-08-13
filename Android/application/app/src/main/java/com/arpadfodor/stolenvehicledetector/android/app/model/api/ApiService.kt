package com.arpadfodor.stolenvehicledetector.android.app.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Vehicle
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.Report
import okhttp3.OkHttpClient

object ApiService{

    private lateinit var stolenVehicleAPI: StolenVehicleAPI

    fun initialize(){

        val httpClient = OkHttpClient.Builder().addInterceptor(
            BasicAuthInterceptor(
                StolenVehicleAPI.DEFAULT_USER,
                StolenVehicleAPI.DEFAULT_USER_PASSWORD
            )
        ).build()

        val retrofitStolenVehiclesAPI = Retrofit.Builder()
            .baseUrl(StolenVehicleAPI.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        this.stolenVehicleAPI = retrofitStolenVehiclesAPI.create(StolenVehicleAPI::class.java)

    }

    fun getVehiclesData(callback: (List<Vehicle>) -> Unit) {

        Thread {
            try {
                val dataCall = stolenVehicleAPI.getVehiclesData()
                val dataResponse = dataCall.execute().body() ?: emptyList<ApiVehicle>()
                val transformedDataResponse = apiVehicleResponseTransform(dataResponse)
                callback(transformedDataResponse)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }.start()

    }

    fun getVehiclesMeta(callback: (Int, String) -> Unit) {

        Thread {
            var size = 0
            //TODO: for testing, should be DateHandler.defaultDate()
            var timestampUTC = "1980-01-01 01:01:01"
            try {
                val metaDataCall = stolenVehicleAPI.getVehiclesMeta()
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

    fun getReportsData(callback: (List<Report>) -> Unit) {

        Thread {
            try {
                val dataCall = stolenVehicleAPI.getReportsData()
                val dataResponse = dataCall.execute().body() ?: emptyList<ApiVehicleReport>()
                val transformedDataResponse = apiReportResponseTransform(dataResponse)
                callback(transformedDataResponse)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }.start()

    }

    fun getReportsMeta(callback: (Int, String) -> Unit) {

        Thread {

            var size = 0
            //TODO: for testing, should be DateHandler.defaultDate()
            var timestampUTC = "1980-01-01 01:01:01"

            try {
                val metaDataCall = stolenVehicleAPI.getReportsMeta()
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

    fun postReport(report: ApiVehicleReport, callback: (Boolean) -> Unit){

        Thread {

            try {
                val postReportCall = stolenVehicleAPI.postReport(report)
                var response = postReportCall.execute().body() ?: ""

                //TODO: response check
                response = "200"
                if(response == "200"){
                    callback(true)
                }
                else{
                    callback(false)
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

        }.start()

    }

    private fun apiVehicleResponseTransform(content: List<ApiVehicle>) : List<Vehicle>{

        val vehiclesList = mutableListOf<Vehicle>()

        for(i in content.indices){

            val current = content[i]

            //TODO: new server will provide data in a better format
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
                vehiclesList.add(constructed)
            }
        }

        //TODO: just for testing
        vehiclesList.add(
            Vehicle(
                "SAMSUNG",
                "phone",
                "Samsung",
                "black"
            )
        )
        vehiclesList.add(
            Vehicle(
                "HJC759",
                "car",
                "Opel",
                "black"
            )
        )

        return vehiclesList

    }

    private fun apiReportResponseTransform(content: List<ApiVehicleReport>) : List<Report>{

        val reportsList = mutableListOf<Report>()

        for(i in content.indices){

            val current = content[i]

            val constructed = Report(current.Id, current.Vehicle, current.Reporter,
                current.latitude, current.longitude, current.message, current.timestampUTC)

            reportsList.add(constructed)

        }

        //TODO: just for testing
        reportsList.add(
            Report(
                1,
                "SAMSUNG",
                "aaa@bbb.com",
                47.519959,
                19.079840,
                "I never thought I will find it there!",
                "2020-07-18 06:00:00"
            )
        )
        reportsList.add(
            Report(
                2,
                "HJC759",
                "aaa@bbb.com",
                47.496686,
                19.039277,
                "Wow! Such a finding!",
                "2020-07-30 16:12:34"
            )
        )

        return reportsList

    }

    //TODO: user, self interactions

}