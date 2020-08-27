package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiReport(
    var Id: Int,
    var Vehicle: String,
    var Reporter: String,
    var latitude: Double,
    var longitude: Double,
    var message: String,
    var timestampUTC: String
)