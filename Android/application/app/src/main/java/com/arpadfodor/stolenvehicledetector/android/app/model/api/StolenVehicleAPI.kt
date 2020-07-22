package com.arpadfodor.stolenvehicledetector.android.app.model.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface StolenVehicleAPI {

    companion object {
        const val API_KEY = "tmPCDRV0eK6e"
        const val API_TOKEN = "tSjKNzYReoFE"
        const val ENDPOINT_URL = "https://www.parsehub.com/api/v2/projects/${API_TOKEN}/"
        const val GET_DATA = "${ENDPOINT_URL}last_ready_run/data/"
        const val GET_META = "${GET_DATA}meta/"
    }

    @GET(GET_DATA)
    fun getData(@Query("api_key") key: String = API_KEY): Call<List<ApiVehicle>>

    @GET(GET_META)
    fun getMetaData(@Query("api_key") key: String = API_KEY): Call<ApiMetaData>

}