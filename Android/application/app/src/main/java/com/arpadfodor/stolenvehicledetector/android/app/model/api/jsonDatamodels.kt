package com.arpadfodor.stolenvehicledetector.android.app.model.api

data class MetaDataJson(
    var dataSize: Int,
    var modificationTimeStampUTC: String
)

data class StolenVehiclesJson(
    val vehicle: List<StolenVehicleJson>
)

data class StolenVehicleJson(
    val name: String,
    val type: String,
    val manufacturer: String,
    val color: String
)