package com.arpadfodor.stolenvehicledetector.android.app.model.api

data class TimestampJson(
    val timestampUTC: String
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