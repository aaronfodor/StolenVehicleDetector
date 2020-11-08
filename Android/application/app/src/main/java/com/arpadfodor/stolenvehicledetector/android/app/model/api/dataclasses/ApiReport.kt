package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiReport(
    var vehicleLicenseId: String,
    var reporterEmail: String,
    var latitude: Double,
    var longitude: Double,
    var message: String,
    var key: String = vehicleLicenseId,
    var version: Int = 1,
    var validFromUTC: String = "",
    var validToUTC: String = ""
)