package com.arpadfodor.android.stolencardetector.model.api

data class StolenVehiclesJson(
    val stolenVehicles: List<StolenVehicleJson>
)

data class StolenVehicleJson(
    val licenseId: String,
    val vehicleType: String,
    val manufacturer: String,
    val color: String
)