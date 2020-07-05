package com.arpadfodor.ktor.model.dataclasses

data class VehicleReports(
    var reports: MutableList<VehicleReport>,
    var meta: MetaData
)