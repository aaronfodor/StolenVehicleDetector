package com.arpadfodor.ktor.model.dataclasses

data class StolenVehicles(
    var vehicles: MutableList<StolenVehicle>,
    var meta: MetaData
)