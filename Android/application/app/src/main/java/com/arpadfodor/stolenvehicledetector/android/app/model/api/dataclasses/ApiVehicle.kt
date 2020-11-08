package com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses

data class ApiVehicle(
    val licenseId: String,
    var type: String,
    var manufacturer: String,
    var color: String,
    var key: String = licenseId,
    var version: Int = 1,
    var validFromUTC: String = "",
    var validToUTC: String = ""
)