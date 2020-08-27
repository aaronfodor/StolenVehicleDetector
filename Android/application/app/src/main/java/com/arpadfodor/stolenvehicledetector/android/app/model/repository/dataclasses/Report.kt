package com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses

data class Report(
    val Id: Int = 0,
    val Vehicle: String = "",
    val Reporter: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val message: String = "",
    val timestampUTC: String = ""
)