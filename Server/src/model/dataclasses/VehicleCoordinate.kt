package com.arpadfodor.ktor.model.dataclasses

data class VehicleCoordinate(
    val vehicleLicensePlate: String,
    var latitude: Double,
    var longitude: Double,
    var detectionTimeStampUTC: String
)