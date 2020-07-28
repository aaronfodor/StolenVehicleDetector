package com.arpadfodor.stolenvehicledetector.android.app.model.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StolenVehicleAPI {

    companion object {
        const val API_KEY = "tmPCDRV0eK6e"
        const val API_TOKEN = "tSjKNzYReoFE"
        const val ENDPOINT_URL = "https://www.parsehub.com/api/v2/projects/${API_TOKEN}/"
        const val GET_VEHICLES = "${ENDPOINT_URL}last_ready_run/data/"
        const val GET_VEHICLES_META = "${GET_VEHICLES}meta/"
        const val POST_REPORT = "${ENDPOINT_URL}report/"
    }

    @GET(GET_VEHICLES)
    fun getData(@Query("api_key") key: String = API_KEY): Call<List<ApiVehicle>>

    @GET(GET_VEHICLES_META)
    fun getMetaData(@Query("api_key") key: String = API_KEY): Call<ApiMetaData>

}