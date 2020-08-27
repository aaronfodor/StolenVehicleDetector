package com.arpadfodor.stolenvehicledetector.android.app.model.api

import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiMetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiReport
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiVehicle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    fun getVehiclesData(callback: (List<ApiVehicle>) -> Unit) {

        Thread {

            try {
                val dataCall = stolenVehicleAPI.getVehiclesData()
                val dataResponse = dataCall.execute().body() ?: emptyList()

                //TODO: just for testing
                val responseWithAddedElements = dataResponse.toMutableList()

                responseWithAddedElements.add(
                    ApiVehicle(
                        "HJC-759",
                        "car",
                        "Opel",
                        "black",
                    )
                )
                responseWithAddedElements.add(
                    ApiVehicle(
                        "Samsung",
                        "device",
                        "Samsung",
                        "many",
                    )
                )
                responseWithAddedElements.add(
                    ApiVehicle(
                        "Acer",
                        "device",
                        "Acer",
                        "many",
                    )
                )

                callback(responseWithAddedElements)
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

    fun getReportsData(callback: (List<ApiReport>) -> Unit) {

        Thread {

            try {
                val dataCall = stolenVehicleAPI.getReportsData()
                val dataResponse = dataCall.execute().body() ?: emptyList()

                //TODO: just for testing
                val responseWithAddedElements = dataResponse.toMutableList()

                responseWithAddedElements.add(
                    ApiReport(
                        1,
                        "SAMSUNG",
                        "aaa@bbb.com",
                        47.519959,
                        19.079840,
                        "I never thought I will find it there!",
                        "2020-07-18 06:00:00"
                    )
                )
                responseWithAddedElements.add(
                    ApiReport(
                        2,
                        "HJC759",
                        "aaa@bbb.com",
                        47.496686,
                        19.039277,
                        "Wow! Such a finding!",
                        "2020-07-30 16:12:34"
                    )
                )

                callback(responseWithAddedElements)
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

    fun postReport(report: ApiReport, callback: (Boolean) -> Unit){

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

    //TODO: user, self interactions

}