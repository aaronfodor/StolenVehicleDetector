package com.arpadfodor.ktor.model.dataclasses

data class Report(
    var Id: Int,
    var Vehicle: String,
    var Reporter: String,
    var latitude: Double,
    var longitude: Double,
    var message: String,
    var timestampUTC: String
)