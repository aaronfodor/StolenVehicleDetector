package com.arpadfodor.ktor.model.dataclasses

data class VehicleCoordinates(
    var coordinates: MutableList<VehicleCoordinate>,
    var meta: MetaData
)