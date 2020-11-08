package com.arpadfodor.stolenvehicledetector.android.app.model.api

import com.arpadfodor.stolenvehicledetector.android.app.model.DateHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiMetaData
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiReport
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiUser
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiVehicle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object ApiService{

    private lateinit var stolenVehicleAPI: StolenVehicleAPI

    fun initialize(httpClient: OkHttpClient){
        val retrofitStolenVehiclesAPI = Retrofit.Builder()
            .baseUrl(StolenVehicleAPI.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
        this.stolenVehicleAPI = retrofitStolenVehiclesAPI.create(StolenVehicleAPI::class.java)
    }

    fun getVehiclesData(callback: (List<ApiVehicle>) -> Unit) {

        Thread {

            var dataResponse: List<ApiVehicle> = listOf()

            try {
                val dataCall = stolenVehicleAPI.getVehiclesData()
                dataResponse = dataCall.execute().body() ?: emptyList()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally{
                callback(dataResponse)
            }

        }.start()

    }

    fun getVehiclesMeta(callback: (Int, String) -> Unit) {

        Thread {

            var size = 0
            var timestampUTC = DateHandler.defaultDateString()

            try {
                val metaDataCall = stolenVehicleAPI.getVehiclesMeta()
                val metaDataResponse = metaDataCall.execute().body()
                    ?: ApiMetaData("", 0, timestampUTC)
                size = metaDataResponse.dataSize
                timestampUTC = metaDataResponse.modificationTimeStampUTC
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(size, timestampUTC)
            }

        }.start()

    }

    fun getReportsData(callback: (List<ApiReport>) -> Unit) {

        Thread {

            var dataResponse: List<ApiReport> = listOf()

            try {
                val dataCall = stolenVehicleAPI.getReportsData()
                dataResponse = dataCall.execute().body() ?: emptyList()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(dataResponse)
            }

        }.start()

    }

    fun getReportsMeta(callback: (Int, String) -> Unit) {

        Thread {

            var size = 0
            var timestampUTC = DateHandler.dateToString(DateHandler.defaultDate())

            try {
                val metaDataCall = stolenVehicleAPI.getReportsMeta()
                val metaDataResponse = metaDataCall.execute().body()
                    ?: ApiMetaData("", 0, timestampUTC)
                size = metaDataResponse.dataSize
                timestampUTC = metaDataResponse.modificationTimeStampUTC
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(size, timestampUTC)
            }

        }.start()

    }

    fun postReport(report: ApiReport, callback: (Boolean) -> Unit){

        Thread {

            var isSuccess = false

            try {
                val postReportCall = stolenVehicleAPI.postReport(report)
                val responseCode = postReportCall.execute().code()
                isSuccess = responseCode < 300
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    fun deleteSelf(success: () -> Unit, error: () -> Unit){

        Thread {

            var isSuccess = false

            try {
                val deleteSelfCall = stolenVehicleAPI.deleteSelf()
                val responseCode = deleteSelfCall.execute().code()
                isSuccess = responseCode < 300
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                if(isSuccess) {
                    success()
                }
                else{
                    error()
                }
            }

        }.start()

    }

    fun postApiUser(email: String, name: String, password: String, success: () -> Unit, error: () -> Unit){

        Thread {

            var isSuccess = false

            try {
                val user = ApiUser(email, password, name, "", true, 0, mutableListOf())
                val postApiUserCall = stolenVehicleAPI.postApiUser(user)
                val responseCode = postApiUserCall.execute().code()
                isSuccess = responseCode < 300
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                if(isSuccess) {
                    success()
                }
                else{
                    error()
                }
            }

        }.start()

    }

    fun putSelf(email: String, nameToSet: String, passwordToSet: String, success: () -> Unit, error: () -> Unit){

        Thread {

            var isSuccess = false

            try {
                val user = ApiUser(email, passwordToSet, nameToSet, "", true, 0, mutableListOf())
                val putSelfCall = stolenVehicleAPI.putSelf(user)
                val responseCode = putSelfCall.execute().code()
                isSuccess = responseCode < 300
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                if(isSuccess) {
                    success()
                }
                else{
                    error()
                }
            }

        }.start()

    }

    fun login(success: () -> Unit, error: () -> Unit){

        Thread {
            var isSuccess = false

            try {
                val metaDataCall = stolenVehicleAPI.login()
                val responseCode = metaDataCall.execute().code()
                isSuccess = responseCode < 300
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                if(isSuccess){
                    success()
                }
                else{
                    error()
                }
            }

        }.start()

    }

}