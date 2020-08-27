package com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses

data class Vehicle(
    val licenseId: String,
    val type: String = "",
    val manufacturer: String = "",
    val color: String = ""
)