package com.arpadfodor.ktor.model.dataclasses

data class VehicleReport(
    val vehicleLicensePlate: String,
    val latitude: Double,
    val longitude: Double,
    val detectionTimeStampUTC: String,
    val message: String
)