package com.arpadfodor.stolenvehicledetector.android.app.model.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StolenVehicleAPI {

    //TODO: new server interaction
    companion object {

        const val BASE_URL = "https://www.mydomain.com/"
        const val API_URL = "${BASE_URL}api/v1/"

        const val DEFAULT_USER = "default_user@stolen_vehicle_detector"
        const val DEFAULT_USER_PASSWORD = "default_user_czka84"

        const val GET_VEHICLES = "${API_URL}vehicles/"
        const val GET_VEHICLES_META = "${GET_VEHICLES}meta/"
        const val GET_REPORTS = "${API_URL}coordinates/"
        const val GET_REPORTS_META = "${GET_REPORTS}meta/"
        const val POST_REPORT = "${API_URL}report/"

    }

    @GET(GET_VEHICLES)
    fun getVehiclesData(): Call<List<ApiVehicle>>

    @GET(GET_VEHICLES_META)
    fun getVehiclesMeta(): Call<ApiMetaData>

    @GET(GET_REPORTS)
    fun getReportsData(): Call<List<ApiVehicleReport>>

    @GET(GET_REPORTS_META)
    fun getReportsMeta(): Call<ApiMetaData>

    @POST(POST_REPORT)
    fun postReport(@Body report: ApiVehicleReport): Call<String>

}