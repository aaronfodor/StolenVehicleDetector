package com.arpadfodor.stolenvehicledetector.android.app.model.api

data class ApiMetaData(
    var dataSize: Int,
    var modificationTimestampUTC: String
)

data class ApiVehicle(
    val name: String,
    val type: String,
    val manufacturer: String,
    val color: String
)

data class ApiVehicleReport(
    var Id: Int,
    var Vehicle: String,
    var Reporter: String,
    var latitude: Double,
    var longitude: Double,
    var message: String,
    var timestampUTC: String
)

data class ApiUser(
    var email: String,
    var name: String,
    var password: String,
    var hint: String,
    var numReports: Int
)