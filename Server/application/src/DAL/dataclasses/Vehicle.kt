package com.arpadfodor.ktor.model.dataclasses

data class Vehicle(
    var licenseId: String,
    var type: String,
    var manufacturer: String,
    var color: String
)