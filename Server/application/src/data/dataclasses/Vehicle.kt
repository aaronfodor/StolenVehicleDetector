package com.arpadfodor.stolenvehicledetector.data.dataclasses

data class Vehicle(
        val licenseId: String,
        var type: String,
        var manufacturer: String,
        var color: String,
        override var key: String = licenseId,
        override var version: Int = 1,
        override var validFromUTC: String = "",
        override var validToUTC: String = ""
) : IDataclass